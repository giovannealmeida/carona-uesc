package br.com.versalius.carona.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Giovanne on 01/07/2016.
 */
public class DBHelper extends SQLiteOpenHelper {

    //Constantes do banco
    private static final String DB_NAME = "carona_uesc_db";
    private static final int DB_VERSION = 5;

    //Constantes das tabelas
    public static final String TBL_SESSION = "session";
    public static final String TBL_VEHICLE = "vehicles";
    public static final String TBL_VEHICLE_GALLERY = "vehicles_gallery";
    public static final String TBL_RIDE_HISTORY = "ride_history";

    private SQLiteDatabase database;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        database = getWritableDatabase();
    }

    public SQLiteDatabase getDatabase() {
        return database;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Criação da tabela de sessão
        db.execSQL("CREATE TABLE " + TBL_SESSION + " (" +
                " email TEXT NOT NULL," +
                " password TEXT NOT NULL," +
                " user_id INTEGER);");

        //Criação da tabela de histórico de caronas
        db.execSQL("CREATE TABLE " + TBL_RIDE_HISTORY + " (" +
                " id INTEGER PRIMARY KEY," +
                " driver_id INTEGER NOT NULL," +
                " origin_id INTEGER NOT NULL," +
                " destination_city TEXT NOT NULL," +
                " destination_neighborhood TEXT NOT NULL," +
                " depart_time TEXT);");

        //Criação da tabela de veículos
        db.execSQL("CREATE TABLE " + TBL_VEHICLE + " (" +
                " id INTEGER PRIMARY KEY," +
                " is_default INTEGER NOT NULL," +
                " type INTEGER NOT NULL," +
                " model TEXT NOT NULL," +
                " brand TEXT NOT NULL," +
                " air INTEGER NOT NULL," +
                " num_doors INTEGER NOT NULL," +
                " num_sits INTEGER NOT NULL," +
                " plate TEXT NOT NULL," +
                " color_name TEXT NOT NULL," +
                " color_hex TEXT NOT NULL," +
                " main_pic_url TEXT);");

        //Criação da tabela de galeria veículos
        db.execSQL("CREATE TABLE " + TBL_VEHICLE_GALLERY + " (" +
                " id INTEGER PRIMARY KEY," +
                " vehicle_id INTEGER NOT NULL," +
                " pic_url TEXT," +
                " FOREIGN KEY(vehicle_id) REFERENCES " + TBL_VEHICLE + "(id) ON DELETE CASCADE);");


//        //Criação da tabela de histórico de caronas
//        db.execSQL("CREATE TABLE " + TBL_QUESTION_ITEM + " (" +
//                " id INTEGER PRIMARY KEY," +
//                " item_pos INTEGER NOT NULL," +
//                " text TEXT NOT NULL," +
//                " question_id INTEGER," +
//                " child_question_id INTEGER," +
//                " FOREIGN KEY(question_id) REFERENCES " + TBL_QUESTION + "(id) ON DELETE CASCADE," +
//                " FOREIGN KEY(child_question_id) REFERENCES " + TBL_QUESTION + "(id) ON DELETE SET NULL);");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TBL_SESSION);
        db.execSQL("DROP TABLE IF EXISTS " + TBL_RIDE_HISTORY);
        db.execSQL("DROP TABLE IF EXISTS " + TBL_VEHICLE);
        db.execSQL("DROP TABLE IF EXISTS " + TBL_VEHICLE_GALLERY);

        onCreate(db);
    }

    /**
     * Deleta todos os registros de todas as tabelas do banco
     */
    public void clearAll() {
        getDatabase().execSQL("DELETE FROM " + DBHelper.TBL_SESSION);
        getDatabase().execSQL("DELETE FROM " + DBHelper.TBL_VEHICLE);
        getDatabase().execSQL("DELETE FROM " + DBHelper.TBL_VEHICLE_GALLERY);
        getDatabase().execSQL("DELETE FROM " + DBHelper.TBL_RIDE_HISTORY);
    }

}
