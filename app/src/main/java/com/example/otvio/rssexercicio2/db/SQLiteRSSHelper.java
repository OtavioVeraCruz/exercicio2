package com.example.otvio.rssexercicio2.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.otvio.rssexercicio2.domain.ItemRSS;

public class SQLiteRSSHelper extends SQLiteOpenHelper {

    //Nome do Banco de Dados
    private static final String DATABASE_NAME = "rss";
    //Nome da tabela do Banco a ser usada
    public static final String DATABASE_TABLE = "items";
    //Versão atual do banco
    private static final int DB_VERSION = 1;

    //alternativa
    Context c;

    private SQLiteRSSHelper(Context context) {
        super(context, DATABASE_NAME, null, DB_VERSION);
        c = context;
    }

    private static SQLiteRSSHelper db;

    //Definindo Singleton
    public static SQLiteRSSHelper getInstance(Context c) {
        if (db==null) {
            db = new SQLiteRSSHelper(c.getApplicationContext());
        }
        return db;
    }

    //Definindo constantes que representam os campos do banco de dados
    public static final String ITEM_ROWID = RssProviderContract._ID;
    public static final String ITEM_TITLE = RssProviderContract.TITLE;
    public static final String ITEM_DATE = RssProviderContract.DATE;
    public static final String ITEM_DESC = RssProviderContract.DESCRIPTION;
    public static final String ITEM_LINK = RssProviderContract.LINK;
    public static final String ITEM_UNREAD = RssProviderContract.UNREAD;

    //Definindo constante que representa um array com todos os campos
    public final static String[] columns = { ITEM_ROWID, ITEM_TITLE, ITEM_DATE, ITEM_DESC, ITEM_LINK, ITEM_UNREAD};

    //Definindo constante que representa o comando de criação da tabela no banco de dados
    private static final String CREATE_DB_COMMAND = "CREATE TABLE " + DATABASE_TABLE + " (" +
            ITEM_ROWID +" integer primary key autoincrement, "+
            ITEM_TITLE + " text not null, " +
            ITEM_DATE + " text not null, " +
            ITEM_DESC + " text not null, " +
            ITEM_LINK + " text not null unique, " +
            ITEM_UNREAD + " boolean not null);";

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Executa o comando de criação de tabela
        db.execSQL(CREATE_DB_COMMAND);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //estamos ignorando esta possibilidade no momento
        throw new RuntimeException("nao se aplica");
    }

    //IMPLEMENTAR ABAIXO
    //Implemente a manipulação de dados nos métodos auxiliares para não ficar criando consultas manualmente
    public void insertItem(ItemRSS item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(ITEM_TITLE, item.getTitle());
        values.put(ITEM_LINK, item.getLink());
        values.put(ITEM_DATE, item.getPubDate());
        values.put(ITEM_DESC, item.getDescription());
        values.put(ITEM_UNREAD, "0");
        db.insertWithOnConflict(DATABASE_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);

    }
    public long insertItem(String title, String pubDate, String description, String link) {

        return (long) 0.0;
    }
    public ItemRSS getItemRSS(String link) throws SQLException {

        SQLiteDatabase db_item = this.getReadableDatabase();
        String[] columns = {ITEM_TITLE, ITEM_LINK, ITEM_DATE, ITEM_DESC, ITEM_UNREAD};
        String selection = ITEM_LINK + " = ?";
        String[] selectionArgs = {link};
        Cursor c = db_item.query(DATABASE_TABLE, columns, selection, selectionArgs, null, null, null);
        ItemRSS aux = null;

       while (c.moveToNext()) {
            String title = c.getString(c.getColumnIndexOrThrow(ITEM_TITLE));
            String date = c.getString(c.getColumnIndexOrThrow(ITEM_DATE));
            String desc = c.getString(c.getColumnIndexOrThrow(ITEM_DESC));
            aux = new ItemRSS(title, link, date, desc);
        }

        return aux;
    }
    public Cursor getItems() throws SQLException {
        String selectQuery = "select * from " + DATABASE_TABLE+" where "+ITEM_UNREAD+" = 0";

        SQLiteDatabase db_read = this.getReadableDatabase();
        Cursor cursor = db_read.rawQuery(selectQuery, null);
        return cursor;
    }
    public boolean markAsUnread(String link) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        ItemRSS item=getItemRSS(link);
        values.put(ITEM_TITLE, item.getTitle());
        values.put(ITEM_LINK, item.getLink());
        values.put(ITEM_DATE, item.getPubDate());
        values.put(ITEM_DESC, item.getDescription());
        values.put(ITEM_UNREAD, "1");
        boolean retorno;
        int aux= db.update(DATABASE_NAME, values, ITEM_LINK + "=" + link, null);
        retorno = aux == 1;

        return retorno;
    }

    public boolean markAsRead(String link) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        ItemRSS item=getItemRSS(link);

        values.put(ITEM_TITLE, item.getTitle());
        values.put(ITEM_LINK, item.getLink());
        values.put(ITEM_DATE, item.getPubDate());
        values.put(ITEM_DESC, item.getDescription());
        values.put(ITEM_UNREAD, "1");

        String whereClause=ITEM_LINK + " = ?";
        boolean retorno;
        int aux= db.update(DATABASE_TABLE, values,whereClause , new String[]{link});
        retorno = aux == 1;

        return retorno;
    }

}

