package com.example.lovelygallery.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.example.lovelygallery.MainActivity;
import com.example.lovelygallery.R;

import java.util.concurrent.Executor;

public class LoginActivity extends AppCompatActivity {

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicjalizacja komponentów
        Button btnFingerprint = findViewById(R.id.btnFingerprint);

        // Sprawdzenie, czy urządzenie obsługuje biometrię
        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                // Urządzenie obsługuje biometrię
                setupBiometricAuthentication();
                btnFingerprint.setOnClickListener(v -> {
                    biometricPrompt.authenticate(promptInfo);
                });
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                // Urządzenie nie ma sensora biometrycznego
                Toast.makeText(this, R.string.no_biometric_features,
                        Toast.LENGTH_LONG).show();
                // Tutaj można dodać alternatywną metodę logowania, np. PIN
                // Dla celów demonstracyjnych, przechodzimy od razu do głównej aktywności
                startMainActivity();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                // Nie skonfigurowano żadnej biometrii
                Toast.makeText(this, "Brak skonfigurowanych danych biometrycznych. " +
                        "Skonfiguruj je w ustawieniach", Toast.LENGTH_LONG).show();
                // Dla celów demonstracyjnych, przechodzimy od razu do głównej aktywności
                startMainActivity();
                break;
            default:
                Toast.makeText(this, "Nieznany błąd biometrii", Toast.LENGTH_LONG).show();
                // Dla celów demonstracyjnych, przechodzimy od razu do głównej aktywności
                startMainActivity();
                break;
        }
    }

    private void setupBiometricAuthentication() {
        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode, CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        Toast.makeText(getApplicationContext(),
                                        getString(R.string.authentication_error) + ": " + errString, Toast.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        Toast.makeText(getApplicationContext(),
                                R.string.authentication_succeeded, Toast.LENGTH_SHORT).show();

                        // Przejście do głównej aktywności
                        startMainActivity();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Toast.makeText(getApplicationContext(), R.string.authentication_failed,
                                Toast.LENGTH_SHORT).show();
                    }
                });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Odblokuj galerię")
                .setSubtitle("Użyj odcisku palca, aby uzyskać dostęp")
                .setNegativeButtonText(getString(R.string.cancel))
                .build();
    }

    private void startMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // zamknięcie aktywności logowania
    }
}