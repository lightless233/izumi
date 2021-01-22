while (1)
{
    Start-Process java.exe -ArgumentList "-Dmirai.no-desktop", "-jar", "./izumi-1.0.1-SNAPSHOT-all.jar" -NoNewWindow -Wait
}