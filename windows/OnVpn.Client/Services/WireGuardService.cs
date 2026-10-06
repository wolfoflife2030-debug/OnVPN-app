using System.Diagnostics;

namespace OnVpn.Client.Services;

public sealed class WireGuardService
{
    private static string? FindWireGuardExe()
    {
        var candidates = new[] {
            Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ProgramFiles), "WireGuard", "wireguard.exe"),
            Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ProgramFilesX86), "WireGuard", "wireguard.exe")
        };
        return candidates.FirstOrDefault(File.Exists);
    }

    private static async Task<string> RunAsync(string exe, string args)
    {
        using var p = new Process { StartInfo = new ProcessStartInfo(exe, args) { UseShellExecute = false, RedirectStandardOutput = true, RedirectStandardError = true, CreateNoWindow = true } };
        p.Start();
        var stdout = await p.StandardOutput.ReadToEndAsync();
        var stderr = await p.StandardError.ReadToEndAsync();
        await p.WaitForExitAsync();
        if (p.ExitCode != 0) throw new InvalidOperationException(string.IsNullOrWhiteSpace(stderr) ? stdout : stderr);
        return stdout;
    }

    public async Task InstallAndStartAsync(string configPath)
    {
        var exe = FindWireGuardExe() ?? throw new FileNotFoundException("WireGuard for Windows غير مثبت. ثبّت النسخة الرسمية أولاً.");
        await RunAsync(exe, $"/installtunnelservice \"{configPath}\"");
    }

    public async Task StopAndUninstallAsync(string configPath)
    {
        var exe = FindWireGuardExe() ?? throw new FileNotFoundException("WireGuard for Windows غير مثبت.");
        var name = Path.GetFileNameWithoutExtension(configPath);
        await RunAsync(exe, $"/uninstalltunnelservice \"{name}\"");
    }

    public async Task<(string Rx, string Tx)?> GetStatsAsync(string configPath)
    {
        var exe = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ProgramFiles), "WireGuard", "wg.exe");
        if (!File.Exists(exe)) return null;
        var name = Path.GetFileNameWithoutExtension(configPath);
        var output = await RunAsync(exe, $"show \"{name}\" transfer");
        var values = output.Split((char[]?)null, StringSplitOptions.RemoveEmptyEntries);
        if (values.Length < 3) return null;
        return (values[^2] + " B", values[^1] + " B");
    }
}
