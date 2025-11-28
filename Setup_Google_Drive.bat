@echo off
echo ============================================
echo    RP KARONGI - GOOGLE DRIVE AUTO-SETUP
echo ============================================
echo.
echo  Setting up automatic Google Drive backup...
echo.
echo  Step 1: Creating backup structure...
if not exist "%USERPROFILE%\RP_Karongi_Backups" mkdir "%USERPROFILE%\RP_Karongi_Backups"
echo  Backup folder created
echo.
echo  Step 2: Please install Google Drive for Desktop:
echo     https://www.google.com/drive/download/
echo.
echo  Step 3: After installation:
echo     1. Open Google Drive
echo     2. Enable 'Backup and Sync'
echo     3. Add '%USERPROFILE%\RP_Karongi_Backups' as sync folder
echo.
echo  Automatic cloud backup will be enabled!
echo.
pause