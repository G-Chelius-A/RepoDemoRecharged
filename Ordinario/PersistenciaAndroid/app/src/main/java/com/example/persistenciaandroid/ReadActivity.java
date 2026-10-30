package com.example.persistenciaandroid;

import android.os.Bundle;
import android.view.Gravity;
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

public class ReadActivity extends AppCompatActivity {
    private final SupabaseRestClient supabase = new SupabaseRestClient();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private TextView statusText;
    private LinearLayout productList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_read);

        statusText = findViewById(R.id.statusText);
        productList = findViewById(R.id.productList);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadProductos();
    }

    private void loadProductos() {
        statusText.setText(R.string.cargando);
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
            row.addView(details, new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1));

            productList.addView(row);
        }
        statusText.setText(getString(R.string.registros_cargados, productos.size()));
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
