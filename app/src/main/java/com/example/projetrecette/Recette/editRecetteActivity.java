package com.example.projetrecette.Recette;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import com.example.projetrecette.Drawer.MesRecettes.mesRecettesActivity;
import com.example.projetrecette.GlideApp;
import com.example.projetrecette.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class editRecetteActivity extends AppCompatActivity {

    Button btn_gluten, btn_arachid, btn_lait, btn_crustace, btn_celeri, btn_fruit, btn_moutarde, btn_poisson;
    Allergie allergie;
    EditText name_recette, temps_prep, temps_cook, multi_ingre, multi_recette;
    RatingBar difficulty;
    ImageView image;
    Button btnEditRecipe, btnDeleteRecipe;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String RecipeId;
    String userId;
    String previousImage;
    StorageReference storageReference;
    StorageTask mUploadTask;
    Uri ImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_recette);
        addToolbar();
        RecipeId = getIntent().getStringExtra("key");
        setAttribut();
        setbtnAllergie();
        onclickAllergie();
        initialisation();
        selectImage();
        onClickEdit();
        onClickDelete();
    }


    public void onClickDelete(){
        this.btnDeleteRecipe = findViewById(R.id.btnDelRecette);

        btnDeleteRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fStore.collection("recipes").document(RecipeId).delete();
                if(!previousImage.equals("default_pic.png")){
                    storageReference.child("Recipes_pics").child(previousImage).delete();
                }
                fStore.collection("users").document(userId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        ArrayList<String> o = (ArrayList<String>) documentSnapshot.getData().get("Mes_Recettes");
                        o.remove(RecipeId);
                        fStore.collection("users").document(userId).update("Mes_Recettes", o);
                    }
                });


                Intent i = new Intent(editRecetteActivity.this, mesRecettesActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });

    }


    public void onClickEdit(){

        btnEditRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = name_recette.getText().toString().trim();
                String temps_prepation = temps_prep.getText().toString().trim();
                String temps_cooking = temps_cook.getText().toString().trim();
                String multi_ingredient = Html.toHtml(multi_ingre.getText());
                String multi_rec =Html.toHtml(multi_recette.getText());

                if(TextUtils.isEmpty(name)){
                    name_recette.setError("Veuillez mettre un nom de recette valide!");
                    return;
                }
                if(TextUtils.isEmpty(temps_cooking)){
                    temps_cooking = "0";
                }

                if (TextUtils.isEmpty(temps_prepation)){
                    temps_prepation = "0";

                }
                if(TextUtils.isEmpty(multi_ingredient)){
                    multi_ingredient = "L'auteur n'a pas donner d'ingr??dient";
                }
                if(TextUtils.isEmpty(multi_rec)){
                    multi_rec = "L'auteur n'a pas donn?? d'??tapes ?? suivre pour la r??alisation de la recette";
                }

                Map<String,Object> recipe = new HashMap<>();
                recipe.put("Nom_Recette", name);
                recipe.put("Temps_Preparation", temps_prepation);
                recipe.put("Temps_Cuisson", temps_cooking);
                recipe.put("Ingredient", multi_ingredient);
                recipe.put("Recette", multi_rec);
                recipe.put("Difficulty", String.valueOf(difficulty.getRating()));
                recipe.put("Allergies", allergie.sendMap());

                fStore.collection("recipes").document(RecipeId).update(recipe);
                uploadFile(RecipeId);
                onBackPressed();

            }
        });


    }

    public void initialisation(){
        fStore.collection("recipes").document(RecipeId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                name_recette.setText(documentSnapshot.getString("Nom_Recette"));
                difficulty.setRating(Float.parseFloat(documentSnapshot.getString("Difficulty")));
                temps_prep.setText(documentSnapshot.getString("Temps_Preparation"));
                temps_cook.setText(documentSnapshot.getString("Temps_Cuisson"));
                multi_ingre.setText(Html.fromHtml(documentSnapshot.getString("Ingredient")));
                multi_recette.setText(Html.fromHtml(documentSnapshot.getString("Recette")));
                previousImage = documentSnapshot.getString("Recipe_Pic");
                Map<String, Boolean> mapAllergie = (Map<String, Boolean>) documentSnapshot.getData().get("Allergies");
                btnAllergieMap(mapAllergie);
                final StorageReference pathphoto = storageReference.child("Recipes_pics").child(documentSnapshot.getString("Recipe_Pic"));
                GlideApp.with(getApplicationContext()).load(pathphoto).into(image);

            }
        });
    }

    public void btnAllergieMap(Map<String, Boolean> map){
        if(map.get("arachid")){
            allergie.arachid = true;
            btn_arachid.setBackgroundColor(Color.parseColor("#fce4ec"));
        }
        if(map.get("crustace")){
            allergie.crustace = true;
            btn_crustace.setBackgroundColor(Color.parseColor("#fce4ec"));
        }
        if(map.get("celeri")){
            allergie.celeri = true;
            btn_celeri.setBackgroundColor(Color.parseColor("#fce4ec"));
        }
        if(map.get("fruitcoq")){
            allergie.fruitcoq = true;
            btn_fruit.setBackgroundColor(Color.parseColor("#fce4ec"));
        }
        if(map.get("gluten")){
            allergie.gluten = true;
            btn_gluten.setBackgroundColor(Color.parseColor("#fce4ec"));
        }
        if(map.get("lait")){
            allergie.lait = true;
            btn_lait.setBackgroundColor(Color.parseColor("#fce4ec"));
        }
        if(map.get("moutarde")){
            allergie.moutarde = true;
            btn_moutarde.setBackgroundColor(Color.parseColor("#fce4ec"));
        }
        if(map.get("poisson")){
            allergie.poisson = true;
            btn_poisson.setBackgroundColor(Color.parseColor("#fce4ec"));
        }



    }

    private void selectImage() {
        final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("Add Photo!");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (options[item].equals("Take Photo")) {
                            /*
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            File f = new File(android.os.Environment.getExternalStorageDirectory(), "temp.jpg");
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                            startActivityForResult(intent, 1);
                            */
                            dialog.dismiss();
                        } else if (options[item].equals("Choose from Gallery")) {
                            Intent intent = new Intent().setType("image/*").setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(intent, 2);
                        } else if (options[item].equals("Cancel")) {
                            dialog.dismiss();
                        }
                    }
                });
                builder.create().show();
            }
        });
    }
    private void uploadFile(final String recipeId) {
        if (ImageUri != null) {
            /* On utilise UUID pour obtenir une id random */
            final StorageReference fileReference = storageReference.child("Recipes_pics").child(UUID.randomUUID().toString()
                    + "." + getFileExtension(ImageUri));

            final DocumentReference drefRecipes = fStore.collection("recipes").document(recipeId);
            mUploadTask = fileReference.putFile(ImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    drefRecipes.update("Recipe_Pic", fileReference.getName());
                    if(!previousImage.equals("default_pic.png")){
                        storageReference.child("Recipes_pics").child(previousImage).delete();
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(),"Erreur", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    public String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            ImageUri = data.getData();
            GlideApp.with(getApplicationContext()).load(ImageUri).into(image);
        }
    }






    private void setAttribut(){
        name_recette = findViewById(R.id.edit_name);
        temps_prep = findViewById(R.id.edit_preparation);
        temps_cook = findViewById(R.id.edit_cooking);
        multi_ingre = findViewById(R.id.edit_ingredient);
        multi_recette = findViewById(R.id.edit_recette);
        difficulty = findViewById(R.id.edit_difficulty);
        btnEditRecipe = findViewById(R.id.btnEditRecette);
        image = findViewById(R.id.edit_image);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userId = fAuth.getCurrentUser().getUid();
        storageReference = FirebaseStorage.getInstance().getReference();
        allergie = new Allergie();
    }



    public void setbtnAllergie(){

        btn_gluten = findViewById(R.id.edit_gluten);
        btn_arachid  = findViewById(R.id.edit_arachide);
        btn_lait = findViewById(R.id.edit_lait);
        btn_crustace = findViewById(R.id.edit_crustace);
        btn_celeri = findViewById(R.id.edit_celeri);
        btn_fruit = findViewById(R.id.edit_fruitcoq);
        btn_moutarde = findViewById(R.id.edit_moutarde);
        btn_poisson = findViewById(R.id.edit_poisson);
    }

    public void onclickAllergie(){
        btn_gluten.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                allergie.setGluten(!allergie.gluten);
                if(allergie.gluten){
                    btn_gluten.setBackgroundColor(Color.parseColor("#fce4ec"));
                }else{
                    btn_gluten.setBackgroundColor(Color.parseColor("#eeeeee"));
                }
            }
        });
        btn_arachid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                allergie.setArachid(!allergie.arachid);
                if(allergie.arachid){
                    btn_arachid.setBackgroundColor(Color.parseColor("#fce4ec"));
                }else{
                    btn_arachid.setBackgroundColor(Color.parseColor("#eeeeee"));
                }
            }
        });
        btn_lait.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                allergie.setLait(!allergie.lait);
                if(allergie.lait){
                    btn_lait.setBackgroundColor(Color.parseColor("#fce4ec"));
                }else{
                    btn_lait.setBackgroundColor(Color.parseColor("#eeeeee"));
                }
            }
        });
        btn_crustace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                allergie.setCrustace(!allergie.crustace);
                if(allergie.crustace){
                    btn_crustace.setBackgroundColor(Color.parseColor("#fce4ec"));
                }else{
                    btn_crustace.setBackgroundColor(Color.parseColor("#eeeeee"));
                }
            }
        });
        btn_celeri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                allergie.setCeleri(!allergie.celeri);
                if(allergie.celeri){
                    btn_celeri.setBackgroundColor(Color.parseColor("#fce4ec"));
                }else{
                    btn_celeri.setBackgroundColor(Color.parseColor("#eeeeee"));
                }
            }
        });
        btn_fruit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                allergie.setFruitcoq(!allergie.fruitcoq);
                if(allergie.fruitcoq){
                    btn_fruit.setBackgroundColor(Color.parseColor("#fce4ec"));
                }else{
                    btn_fruit.setBackgroundColor(Color.parseColor("#eeeeee"));
                }
            }
        });
        btn_moutarde.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                allergie.setMoutarde(!allergie.moutarde);
                if(allergie.moutarde){
                    btn_moutarde.setBackgroundColor(Color.parseColor("#fce4ec"));
                }else{
                    btn_moutarde.setBackgroundColor(Color.parseColor("#eeeeee"));
                }
            }
        });
        btn_poisson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                allergie.setPoisson(!allergie.poisson);
                if(allergie.poisson){
                    btn_poisson.setBackgroundColor(Color.parseColor("#fce4ec"));
                }else{
                    btn_poisson.setBackgroundColor(Color.parseColor("#eeeeee"));
                }
            }
        });

    }

    public void addToolbar(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}