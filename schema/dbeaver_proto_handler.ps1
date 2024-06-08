# Define the custom protocol name and the path to DBeaver executable
$ProtocolName = "dbeaver"
$DBeaverPath = "C:\Path\To\DBeaver.exe"

# Register the custom protocol
New-Item -Path "HKCU:\Software\Classes\$ProtocolName" -Force | Out-Null
New-ItemProperty -Path "HKCU:\Software\Classes\$ProtocolName" -Name "" -Value "URL:$ProtocolName Protocol" -PropertyType String -Force | Out-Null
New-Item -Path "HKCU:\Software\Classes\$ProtocolName\shell\open\command" -Force | Out-Null
New-ItemProperty -Path "HKCU:\Software\Classes\$ProtocolName\shell\open\command" -Name "" -Value "`"$DBeaverPath`" --open-sql-editor --sql-query "%1"" -PropertyType String -Force | Out-Null

Write-Host "Custom protocol $ProtocolName has been registered and associated with DBeaver."
