package br.com.gpaengenharia.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import br.com.gpaengenharia.R;

/**
 * Activity for manage users and teams
 */
public class AtvUsuarios extends Activity {

    @Override
    protected void onRestart() {
        if (AtvLogin.usuario == null)
            startActivityIfNeeded(new Intent(this, AtvLogin.class), 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AtvLogin.usuario == null)
            startActivityIfNeeded(new Intent(this, AtvLogin.class), 0);
        setContentView(R.layout.atv_usuarios);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.atv_usuarios, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
