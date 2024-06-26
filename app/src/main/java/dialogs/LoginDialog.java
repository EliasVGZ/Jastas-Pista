package dialogs;

import static android.app.PendingIntent.getActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.proyecto_eventos.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginDialog extends DialogFragment {

    private EditText usernameInput, passwordInput;
    private Button submitButton;
    private TextView  tv_cuenta_creada;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_login, null);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        usernameInput = view.findViewById(R.id.username_input);
        passwordInput = view.findViewById(R.id.password_input);
        submitButton = view.findViewById(R.id.submit_button);
        tv_cuenta_creada = view.findViewById(R.id.tv_cuenta_creada);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = usernameInput.getText().toString();
                String password = passwordInput.getText().toString();
                if(email.isEmpty() || password.isEmpty()){
                    Toast.makeText(getActivity(), getString(R.string.empty_fields), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!email.contains("@")) {
                    Toast.makeText(getActivity(), getString(R.string.invalid_email), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(getActivity(), getString(R.string.short_password), Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {//se ejecuta cuando se completa la tarea
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {//task es la tarea que se ha completado
                                if (task.isSuccessful()) {
                                    // consigo el UID
                                    String uid = mAuth.getCurrentUser().getUid();

                                    // aqui se crea un nuevo documento con el UID del usuario y de rol usuario.
                                    Map<String, Object> user = new HashMap<>();
                                    user.put("rol", "usuario");

                                    db.collection("usuarios").document(uid)
                                            .set(user)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(getActivity(), getString(R.string.usuario_creado), Toast.LENGTH_SHORT).show();
                                                    dismiss();//
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(getActivity(), getString(R.string.error_usuario), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                } else {
                                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                        Toast.makeText(getActivity(), getString(R.string.usuario_existe), Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getActivity(), getString(R.string.error_usuario), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });
            }
        });

        tv_cuenta_creada.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();//cerrar el dialogo
                IniciarSesionDialog iniciarSesionDialog = new IniciarSesionDialog();
                iniciarSesionDialog.show(getFragmentManager(), "iniciar sesion dialog");
            }
        });

        builder.setView(view)
                .setTitle(R.string.registro)
                .setNegativeButton("cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });

        return builder.create();
    }
}