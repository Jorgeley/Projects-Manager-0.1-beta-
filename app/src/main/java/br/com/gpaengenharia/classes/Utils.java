package br.com.gpaengenharia.classes;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.ViewFlipper;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import br.com.gpaengenharia.R;
import br.com.gpaengenharia.beans.Equipe;

/**
 * Utils for project
 */
public class Utils{
    public static Context contexto;

    /** @param contexto activity context
     */
    public Utils(Context contexto){
        this.contexto = contexto;
    }

    /** overload method to show/hide the progressbar
     * @param barraProgresso
     * @param mostra
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void barraProgresso(final ProgressBar barraProgresso, final boolean mostra) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int animacaoCurta = contexto.getResources().getInteger(android.R.integer.config_shortAnimTime);
            barraProgresso.setVisibility(mostra ? View.VISIBLE : View.GONE);
            barraProgresso.animate().setDuration(animacaoCurta).alpha(
                    mostra ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    barraProgresso.setVisibility(mostra ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            barraProgresso.setVisibility(mostra ? View.VISIBLE : View.GONE);
        }
    }

    /** overload method to show/hide the progressbar
     * @param contexto
     * @param barraProgresso
     * @param mostra
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public static void barraProgresso(Context contexto, final ProgressBar barraProgresso, final boolean mostra) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int animacaoCurta = contexto.getResources().getInteger(android.R.integer.config_shortAnimTime);
            if (barraProgresso != null) {
                barraProgresso.setVisibility(mostra ? View.VISIBLE : View.GONE);
                barraProgresso.animate().setDuration(animacaoCurta).alpha(
                        mostra ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        barraProgresso.setVisibility(mostra ? View.VISIBLE : View.GONE);
                    }
                });
            }
        } else {
            if (barraProgresso != null)
                barraProgresso.setVisibility(mostra ? View.VISIBLE : View.GONE);
        }
    }

    /** overload method to create spinner adapter
     * @param lista Array of strings
     * @return  adaptador of String to Spinner
     */
    public ArrayAdapter setAdaptador(String[] lista){
        ArrayAdapter adaptador = new ArrayAdapter<String>(contexto, android.R.layout.simple_spinner_item, lista){
            @Override
            public boolean isEnabled(int posicao){
                return (posicao == 0) ? false : true;
            }
        };
        return adaptador;
    }

    /** overload method to create spinner adapter
     * @param contexto
     * @param lista Array of strings
     * @return  adaptador of String to Spinner
     */
    public static ArrayAdapter setAdaptador(Context contexto, String[] lista){
        ArrayAdapter adaptador = new ArrayAdapter<String>(contexto, android.R.layout.simple_spinner_item, lista){
            @Override
            public boolean isEnabled(int posicao){
                return (posicao == 0) ? false : true;
            }
        };
        return adaptador;
    }

    /** Creates dialog to DatePicker
     */
    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
        Listener listener;

        public interface Listener{
            /** get the choosen date
             * @return data */
            public void getData(String data);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            //get current date
            final Calendar calendario = Calendar.getInstance();
            int dia = calendario.get(Calendar.DAY_OF_MONTH);
            int mes = calendario.get(Calendar.MONTH);
            int ano = calendario.get(Calendar.YEAR);
            listener = (Listener) getActivity();
            return new DatePickerDialog(getActivity(), this, ano, mes, dia);
        }

        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            //get the choosen date on DatePicker
            Calendar calendario = Calendar.getInstance();
            calendario.set(year, month, day);
            //format the date
            SimpleDateFormat dataFormatada = new SimpleDateFormat("dd/MM/yyyy");
            String data = dataFormatada.format(calendario.getTime());
            if (listener != null)
                listener.getData(data);
        }
    }

    /**
     *  detect fling movements(flip between screens)
     * @param event Evento onTouch of activity
     * @param viewFlipper viewFlipper tha makes the wizard
     * @param layoutEsquerda the view layout to flip to left
     * @param layoutDireita the view layout to flip to right
     * @return ok
     */
    public static boolean onTouchEvent(MotionEvent event, ViewFlipper viewFlipper, View layoutEsquerda, View layoutDireita){
        /*Log.i("classes",
                "MotionEvent: "+event.getClass().toString()+"\n"
                +"ViewFlipper: "+viewFlipper.getClass().toString()+"\n"
                +"ViewEsquerda: "+layoutEsquerda.getClass().toString()+"\n"
                +"ViewDireita: "+layoutDireita.getClass().toString());*/
        float x1 = 0, x2, y1, y2; //coordenadas de touchEvent
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:{
                x1 = event.getX();
                y1 = event.getY();
                break;
            }
            case MotionEvent.ACTION_UP: {
                x2 = event.getX();
                y2 = event.getY();
                //flip layouts left to right
                if (x1 < x2) {
                    //if on dashboard, not flip
                    if (viewFlipper.getCurrentView().getId() == layoutEsquerda.getId())
                        break;
                    viewFlipper.setInAnimation(contexto, R.anim.entra_esquerda);
                    viewFlipper.setOutAnimation(contexto, R.anim.sai_direita);
                    viewFlipper.setDisplayedChild(viewFlipper.indexOfChild(layoutEsquerda));//showNext()
                }else if (x1 > x2) {//flip layouts right to left
                    //if on task layout, not flip
                    if (viewFlipper.getCurrentView().getId() == layoutDireita.getId())
                        break;
                    deslizaLayoutDireita(viewFlipper, layoutDireita);
                }
                break;
            }
                /*//fliping up to down
                if (y1 < y2){
                    Toast.makeText(this, "UP to Down Swap Performed", Toast.LENGTH_LONG).show();
                }
                //fliping down to up
                if (y1 > y2){
                    Toast.makeText(this, "Down to UP Swap Performed", Toast.LENGTH_LONG).show();
                }
                break;*/
        }
        return false;
    }

    /**
     * flip layout to right
     * @param viewFlipper the viewFlipper that makes the wizard
     * @param layoutDireita the layout to flip to right
     */
    public static void deslizaLayoutDireita(ViewFlipper viewFlipper, View layoutDireita){
        viewFlipper.setInAnimation(contexto, R.anim.entra_direita);
        viewFlipper.setOutAnimation(contexto, R.anim.sai_esquerda);
        viewFlipper.setDisplayedChild(viewFlipper.indexOfChild(layoutDireita));//showPrevious();
    }

    /**
     * flip layout to right
     * @param viewFlipper the viewFlipper that makes the wizard
     * @param layoutEsquerda the layout to flip to left
     */
    public static void deslizaLayoutEsquerda(ViewFlipper viewFlipper, View layoutEsquerda){
        viewFlipper.setInAnimation(contexto, R.anim.entra_esquerda);
        viewFlipper.setOutAnimation(contexto, R.anim.sai_direita);
        viewFlipper.setDisplayedChild(viewFlipper.indexOfChild(layoutEsquerda));//showPrevious();
    }

}
