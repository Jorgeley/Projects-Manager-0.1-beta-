package br.com.gpaengenharia.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import br.com.gpaengenharia.R;

/**
 * Activity colaborator (common user)
 */
public class AtvColaborador extends AtvBase{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.atv_colaborador);
        this.setViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = super.criaMenu(menu);
        inflater.inflate(R.menu.atv_colaborador, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){

        }
        return super.onOptionsItemSelected(item);
    }

}
