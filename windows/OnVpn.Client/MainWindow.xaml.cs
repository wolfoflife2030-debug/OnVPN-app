using Microsoft.Win32;
using OnVpn.Client.Services;
using System.IO;
using System.Windows;
using System.Windows.Threading;

namespace OnVpn.Client;

public partial class MainWindow : Window
{
    private readonly WireGuardService _vpn = new();
    private readonly DispatcherTimer _timer = new() { Interval = TimeSpan.FromSeconds(2) };
    private string? _config;
    private bool _connected;

    public MainWindow()
    {
        InitializeComponent();
        _timer.Tick += async (_, _) => await RefreshAsync();
    }

    private void Import_Click(object sender, RoutedEventArgs e)
    {
        var dialog = new OpenFileDialog { Filter = "WireGuard configuration (*.conf)|*.conf" };
        if (dialog.ShowDialog() != true) return;
        _config = dialog.FileName;
        ConfigText.Text = Path.GetFileName(_config);
    }

    private async void Connect_Click(object sender, RoutedEventArgs e)
    {
        try
        {
            if (_config is null) throw new InvalidOperationException("اختر ملف WireGuard أولاً.");
            if (!_connected)
            {
                await _vpn.InstallAndStartAsync(_config);
                _connected = true;
                StatusText.Text = "متصل";
                ConnectButton.Content = "فصل";
                _timer.Start();
            }
            else
            {
                await _vpn.StopAndUninstallAsync(_config);
                _connected = false;
                StatusText.Text = "غير متصل";
                ConnectButton.Content = "اتصال";
                _timer.Stop();
                StatsText.Text = "RX 0 B  •  TX 0 B";
            }
        }
        catch (Exception ex)
        {
            MessageBox.Show(ex.Message, "On Vpn", MessageBoxButton.OK, MessageBoxImage.Error);
        }
    }

    private async Task RefreshAsync()
    {
        if (_config is null) return;
        var stats = await _vpn.GetStatsAsync(_config);
        if (stats is not null) StatsText.Text = $"RX {stats.Value.Rx}  •  TX {stats.Value.Tx}";
    }
}
