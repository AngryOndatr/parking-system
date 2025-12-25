# Generate correct BCrypt hash for parking123

Write-Host "`nЗапуск Java для генерации BCrypt хэша..." -ForegroundColor Cyan

$javaCode = @"
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class HashGen {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
        String password = "parking123";
        String hash = encoder.encode(password);
        System.out.println("NEW_HASH_FOR_parking123:" + hash);
    }
}
"@

# Save Java file
$javaCode | Out-File -FilePath "C:\Temp\HashGen.java" -Encoding UTF8

# Compile and run
Write-Host "Compiling..." -ForegroundColor Yellow
javac -cp "C:\Users\user\.m2\repository\org\springframework\security\spring-security-crypto\6.5.7\spring-security-crypto-6.5.7.jar" C:\Temp\HashGen.java

Write-Host "Running..." -ForegroundColor Yellow
$output = java -cp "C:\Temp;C:\Users\user\.m2\repository\org\springframework\security\spring-security-crypto\6.5.7\spring-security-crypto-6.5.7.jar;C:\Users\user\.m2\repository\org\springframework\spring-core\6.2.14\spring-core-6.2.14.jar;C:\Users\user\.m2\repository\org\springframework\spring-jcl\6.2.14\spring-jcl-6.2.14.jar" HashGen

$hash = ($output | Select-String "NEW_HASH_FOR").ToString().Split(":")[1]

Write-Host "`nNew BCrypt hash for 'parking123':" -ForegroundColor Green
Write-Host $hash -ForegroundColor White

Write-Host "`nUpdating database..." -ForegroundColor Cyan
docker exec parking_db psql -U postgres -d parking_db -c "UPDATE users SET password_hash = '$hash' WHERE username = 'admin';"

Write-Host "`nDone! Test login now." -ForegroundColor Green

