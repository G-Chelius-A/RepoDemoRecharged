package com.example.persistenciaandroid;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UpdateActivity extends AppCompatActivity {
    private final SupabaseRestClient supabase = new SupabaseRestClient();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private EditText productInput;
    private EditText descriptionInput;
    private EditText priceInput;
    private Button saveButton;
    private TextView statusText;
    private LinearLayout productList;
    private String selectedId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update);

        productInput = findViewById(R.id.productInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        priceInput = findViewById(R.id.priceInput);
        saveButton = findViewById(R.id.saveButton);
        statusText = findViewById(R.id.statusText);
        productList = findViewById(R.id.productList);

        saveButton.setOnClickListener(view -> updateProducto());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadProductos();
    }

    private void loadProductos() {
        executor.execute(() -> {
            try {
                List<Producto> productos = supabase.getProductos();
                runOnUiThread(() -> showProductos(productos));
            } catch (Exception exception) {
                showError(exception);
            }
        });
    }

    private void showProductos(List<Producto> productos) {
        productList.removeAllViews();
        for (Producto producto : productos) {
            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setOrientation(LinearLayout.HORIZONTAL);

            TextView details = new TextView(this);
            details.setPadding(0, 12, 8, 12);
            details.setText(String.format(Locale.getDefault(), "%s — $%.2f\n%s",
                    producto.producto, producto.precio, producto.descripcion));
            details.setOnClickListener(view -> selectProducto(producto));
            row.addView(details, new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1));

            Button selectBtn = new Button(this);
            selectBtn.setText(R.string.editar);
            selectBtn.setOnClickListener(view -> selectProducto(producto));
            row.addView(selectBtn);

            productList.addView(row);
        }
    }

    private void selectProducto(Producto producto) {
        selectedId = producto.id;
        productInput.setText(producto.producto);
        descriptionInput.setText(producto.descripcion);
        priceInput.setText(String.format(Locale.getDefault(), "%.2f", producto.precio));
        saveButton.setEnabled(true);
        saveButton.setText(R.string.guardar_cambios);
        statusText.setText(R.string.editando);
    }

    private void updateProducto() {
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

        Producto productoActualizar = new Producto(selectedId, producto, descripcion, precio);
        statusText.setText(R.string.guardando);
        executor.execute(() -> {
            try {
                supabase.actualizarProducto(productoActualizar);
                runOnUiThread(() -> {
                    statusText.setText(R.string.guardado);
                    clearForm();
                    loadProductos();
                });
            } catch (Exception exception) {
                showError(exception);
            }
        });
    }

    private void clearForm() {
        selectedId = null;
        productInput.setText("");
        descriptionInput.setText("");
        priceInput.setText("");
        saveButton.setEnabled(false);
        saveButton.setText(R.string.guardar_cambios);
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
