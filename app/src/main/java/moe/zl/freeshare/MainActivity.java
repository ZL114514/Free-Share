
package moe.zl.freeshare;

import android.view.Window;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.google.android.material.internal.EdgeToEdgeUtils;
import moe.zl.freeshare.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate and get instance of binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        // set content view to binding's root
        setContentView(binding.getRoot());
        
        EdgeToEdgeUtils.applyEdgeToEdge(this.getWindow(),true);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.binding = null;
    }
}
