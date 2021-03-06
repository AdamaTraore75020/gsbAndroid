package fr.yamishadow.gsbandroid.vue;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import fr.yamishadow.gsbandroid.R;
import fr.yamishadow.gsbandroid.controleur.Controle;
import fr.yamishadow.gsbandroid.modele.LoginRequest;
import fr.yamishadow.gsbandroid.modele.Utilisateur;

public class MainActivity extends Activity {
    Button connexion;
    EditText editLogin, editMdp;
    private String TAG = "Login...";
    private Controle controle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        controle = Controle.getInstance(this);
        editLogin = (EditText) findViewById(R.id.identifiant);
        editMdp = (EditText) findViewById(R.id.password);
        connexion = (Button) findViewById(R.id.buttonConnexion);
        connexion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });
    }

    /**
     * Methode qui se chargera de la connexion
     */
    private void login() {
        Log.d(TAG, "btnLogin");
        if(!validate()){
            return;
        }else{
            String login = editLogin.getText().toString();
            String mdp = editMdp.getText().toString();
            Log.d(TAG, login+" "+mdp);
            try {
                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.d(TAG, "test in response");
                            String[] reponseLogin = response.split("%");
                            JSONObject jsonObject = new JSONObject(reponseLogin[1]);
                            Log.d(TAG, "Connexion en cours..." );
                            boolean success = jsonObject.getBoolean("success");
                            if (success) {
                                String login = jsonObject.getString("login");
                                String nom = jsonObject.getString("nom");
                                String id = jsonObject.getString("id");
                                String prenom = jsonObject.getString("prenom");
                                controle.setUser(new Utilisateur(id, nom, prenom, login));
                                Intent intent = new Intent(MainActivity.this, MenuActivity.class);
                                intent.putExtra("login", login);
                                intent.putExtra("nom", nom);
                                intent.putExtra("id", id);
                                intent.putExtra("prenom", prenom);
                                startActivity(intent);
                                finish();
                            } else {
                                Log.d(TAG, "Failed");
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setMessage("Identifiant ou mot de passe incorrect")
                                        .setNegativeButton("Réessayer", null)
                                        .create()
                                        .show();
                            }
                            connexion.setEnabled(true);
                        }catch (ArrayIndexOutOfBoundsException ae){
                            ae.printStackTrace();
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                LoginRequest loginRequest = new LoginRequest(login, mdp, responseListener);
                RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
                queue.add(loginRequest);
            }catch (Exception e){
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Echec de la connexion")
                        .setPositiveButton("Réessayer", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                return;
                            }
                        });
                Dialog dialog = builder.create();
                dialog.show();
            }
        }
    }

    /**
     * Methode qui teste la validité du format des identifiants de connexion
     * @return
     */
    private boolean validate() {
        boolean valid = true;
        String email = editLogin.getText().toString();
        String mdp = editMdp.getText().toString();

        // Teste si l'identifiant est valide ou pas
        if(email.isEmpty()){
            editLogin.setError("Entrez un identifiant valide");
            valid = false;
        }else{
            editLogin.setError(null);
        }

        // teste si le mot de passe est vide ou si il n'est pas conforme aux limites posées
        if(mdp.isEmpty() || mdp.length() <= 2 || mdp.length() >= 16){
            editMdp.setError("Mot de passe compris entre 2 et 16 caractères");
            valid = false;
        }else{
            editMdp.setError(null);
        }
        return valid;
    }
}

