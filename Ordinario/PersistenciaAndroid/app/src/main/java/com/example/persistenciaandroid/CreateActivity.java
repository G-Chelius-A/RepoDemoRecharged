package com.example.persistenciaandroid;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateActivity extends AppCompatActivity {
    private final SupabaseRestClient supabase = new SupabaseRestClient();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private EditText productInput;
    private EditText descriptionInput;
    private EditText priceInput;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create);

        productInput = findViewById(R.id.productInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        priceInput = findViewById(R.id.priceInput);
        Button saveButton = findViewById(R.id.saveButton);
        statusText = findViewById(R.id.statusText);

        saveButton.setOnClickListener(view -> saveProducto());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void saveProducto() {
        String producto = productInput.getText().toString().trim();
        String descripcion = descriptionInput.getText().toString().trim();
        String precioTexto = priceInput.getText().toString().trim().replace(',', '.');

        if (TextUtils.isEmpty(producto) || TextUtils.isEmpty(descripcion)
                || TextUtils.isEmpty(precioTexto)) {
            statusText.setText(R.string.error_campos_requeridos);
            return;
        }

        final double precio;
        try {
            precio = Double.parseDouble(precioTexto);
            if (!Double.isFinite(precio)) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException exception) {
            statusText.setText(R.string.error_precio);
            return;
        }

        Producto productoGuardar = new Producto(null, producto, descripcion, precio);
        statusText.setText(R.string.guardando);
        executor.execute(() -> {
            try {
                supabase.crearProducto(productoGuardar);
                runOnUiThread(() -> {
                    statusText.setText(R.string.guardado);
                    productInput.setText("");
                    descriptionInput.setText("");
                    priceInput.setText("");
                });
            } catch (Exception exception) {
                showError(exception);
            }
        });
    }

    private void showError(Exception exception) {
        runOnUiThread(() -> statusText.setText(
                getString(R.string.error_supabase, exception.getMessage())));
    }

    @Override
    protected void onDestroy() {
        executor.shutdown();
        super.onDestroy();
    }
}
