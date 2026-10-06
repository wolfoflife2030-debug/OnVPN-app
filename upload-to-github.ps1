# One-shot uploader: pushes the whole project (including hidden .github and .gitignore)
# to GitHub and starts the Android + Windows build by pushing a release tag.
$ErrorActionPreference = "Stop"
$Repo = "https://github.com/wolfoflife2030-debug/ONVPN-1.git"
$Tag  = "v0.6.0"

Set-Location $PSScriptRoot

function Run([string]$what, [scriptblock]$cmd) {
    & $cmd
    if ($LASTEXITCODE -ne 0) { throw "FAILED: $what (exit code $LASTEXITCODE)" }
}

if (-not (Get-Command git -ErrorAction SilentlyContinue)) {
    Write-Host "Git is not installed. Opening the download page..." -ForegroundColor Yellow
    Start-Process "https://git-scm.com/download/win"
    Write-Host "Install Git (keep the defaults), then run this script again." -ForegroundColor Yellow
    exit 1
}

if (-not (Test-Path ".git")) { Run "git init" { git init -q } }
if (-not (git config user.name))  { git config user.name  "wolfoflife2030-debug" }
if (-not (git config user.email)) { git config user.email "wolfoflife2030-debug@users.noreply.github.com" }

Run "git add" { git add -A }
git diff --cached --quiet
if ($LASTEXITCODE -ne 0) { Run "git commit" { git commit -q -m "On Vpn $Tag" } }
Run "branch" { git branch -M main }

git remote remove origin 2>$null
Run "remote" { git remote add origin $Repo }

Write-Host "Pushing code (a GitHub sign-in window may appear)..." -ForegroundColor Cyan
Run "push main" { git push -u origin main --force }

Write-Host "Starting the build by pushing tag $Tag ..." -ForegroundColor Cyan
Run "tag" { git tag -f $Tag }
Run "push tag" { git push origin $Tag --force }

Write-Host ""
Write-Host "DONE. Watch the build here:" -ForegroundColor Green
Write-Host "  https://github.com/wolfoflife2030-debug/ONVPN-1/actions"
Write-Host "In ~10 minutes the APK and EXE appear here:" -ForegroundColor Green
Write-Host "  https://github.com/wolfoflife2030-debug/ONVPN-1/releases"
