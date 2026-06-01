$ffmpeg = "C:\Users\janme\Downloads\ffmpeg\ffmpeg\bin\ffmpeg.exe"
$dirs = @("app\src\main\res\drawable-nodpi", "app\src\main\res\drawable")
foreach ($dir in $dirs) {
    if (Test-Path $dir) {
        $files = Get-ChildItem -Path $dir -Include *.png,*.jpg,*.jpeg -Recurse
        foreach ($file in $files) {
            $out = [System.IO.Path]::ChangeExtension($file.FullName, ".webp")
            & $ffmpeg -y -i $file.FullName -c:v libwebp -lossless 0 -q:v 75 $out
            if ($LASTEXITCODE -eq 0) {
                Remove-Item $file.FullName
            }
        }
    }
}
