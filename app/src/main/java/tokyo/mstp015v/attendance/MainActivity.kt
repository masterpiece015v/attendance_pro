package tokyo.mstp015v.attendance

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.*
import io.realm.Realm
import io.realm.kotlin.where
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tokyo.mstp015v.attendance.databinding.ActivityMainBinding
import tokyo.mstp015v.attendance.realm.Student
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private lateinit var menu : Menu
    var client: GoogleSignInClient?= null
    var sheet_id : String? = null
    //var pref : SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView( binding.root )

        //realm
        val realm = Realm.getDefaultInstance()
        var realmresult = realm.where<Student>().findAll()

        //toolbarの設定
        setSupportActionBar( binding.toolbar )
        //supportActionBar?.setDisplayHomeAsUpEnabled(true )

        //drawerの設定
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController
        val appBarConf = AppBarConfiguration.Builder(navController.graph).setOpenableLayout(binding.drawerLayout).build()
        NavigationUI.setupWithNavController(binding.toolbar , navController,appBarConf)
        //NavigationUI.setupActionBarWithNavController(this,navController,binding.drawerLayout)
        //Drawerのmenu_itemをタップした時のイベントを登録する
        binding.navView.setNavigationItemSelectedListener {
            true
        }

        //シートIDをsharedpreferenceから取得する
        val pref = getPreferences(Context.MODE_PRIVATE)
        sheet_id = pref.getString("Sheet_id",null )
        Log.d( "sheet_id" , sheet_id?:"sheet_id null" )

        binding.navView.getHeaderView(0).findViewById<TextView>(R.id.textAccountName).text = "no sign in"

    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.navHostFragment)
        return NavigationUI.navigateUp(navController,binding.drawerLayout)
    }

    //オプションメニューを作る
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate( R.menu.main_menu ,menu)
        this.menu = menu!!
        return true
    }

    //オプションを選択した時
    override fun onOptionsItemSelected(item: MenuItem): Boolean
        =when( item.itemId){
            //バックアップをタップ
            R.id.main_menu_item_backup->{
                //サインインをしてバックアップを取った後、サインアウトする
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestScopes(Scope(DriveScopes.DRIVE_FILE))
                    .requestEmail()
                    .build()
                client = GoogleSignIn.getClient(this, gso)
                val intent = client!!.signInIntent
                backup_launcher.launch(intent)
                true
            }
            //リストアをタップ
            R.id.main_menu_item_restore->{
                //サインインをしてリストアをした後、サインアウトする
                true
            }
            else -> {
                true
            }
    }

    //バックアップリストアのコールバックメソッド
    val backup_launcher = registerForActivityResult( ActivityResultContracts.StartActivityForResult()){
        val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)

        task.addOnSuccessListener {
            val credential = GoogleAccountCredential.usingOAuth2(this,
                Collections.singleton(DriveScopes.DRIVE_FILE))

            credential.selectedAccount = it.account

            //コルーチン
            MainScope().launch{
                //GoogleDriveへのアクセス
                val drive = Drive.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    GsonFactory(),
                    credential
                ).setApplicationName("Drive API").build()

                var pageToken:String? = null
                //非同期通信
                withContext( Dispatchers.Default ){
                    do {
                        val list = drive.files().list()
                            .setQ("name contains 'attendance' and trashed = false and mimeType='application/vnd.google-apps.folder'")
                            .setSpaces("drive")
                            .setFields("nextPageToken,files(id,name,mimeType,modifiedTime)")
                            .setPageToken(pageToken)
                            .execute()

                        Log.d("file_size" , list.files.size.toString() )

                        if( list.files.size > 0 ) {
                            //attendanceが複数ある場合、最も新しいファイルidを取得する
                            var maxdate: DateTime? = null
                            var f_flg = false
                            list.files.forEach {
                                if( f_flg == false){
                                    sheet_id = it.id
                                    f_flg = true
                                }

                                if (maxdate != null && it.modifiedTime.value > maxdate!!.value) {
                                    maxdate = it.modifiedTime
                                    sheet_id = it.id
                                }
                                Log.d("file",
                                    "${it.name},${it.id},${it.modifiedTime},${it.mimeType}")
                            }
                        }else{
                            //attendanceがないので、新しく作る
                            val folder_id = newAttendanceFolder(credential,drive)
                            Log.d("folder_id" , folder_id )
                            sheet_id = newAttendanceSheet(credential,drive,folder_id)
                        }
                    }while( pageToken != null)
                    //val pref = getPreferences(Context.MODE_PRIVATE)
                    //pref?.edit().apply{
                    //    this?.putString("sheet_id",sheet_id )
                    //    this?.commit()
                    //}

                    //Log.d( "pref",pref?.getString("sheet_id" , "not")!! )

                }


            }

        }.addOnFailureListener {
            Log.d("launch","失敗")
        }

    }
    fun newAttendanceFolder( credential:GoogleAccountCredential,drive : Drive):String{

        val filemetadata = File()
        filemetadata.name = "attendance"
        filemetadata.mimeType = "application/vnd.google-apps.folder"

        val file = drive.files().create( filemetadata ).setFields("id, name,webContentLink,webViewLink").execute()
        return file.id

    }
    //新しくattendanceシートを作る
    fun newAttendanceSheet( credential : GoogleAccountCredential,drive:Drive,folder_id:String ) : String{
        val folders = mutableListOf<String>()
        folders.add( folder_id )
        val filemetadata = File()
        filemetadata.name = "attendance"
        filemetadata.mimeType = "application/vnd.google-apps.spreadsheet"
        filemetadata.parents = folders
        val file = drive.files().create( filemetadata ).setFields("id,name").execute()

        val sheet_id = file.id

        val service = Sheets.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory(),
            credential
        ).setApplicationName("Sheets API")
            .build()

        val sheetNames = arrayListOf("student","group","attendance","timetable")
        //val sheetList = mutableListOf<Sheet>()

        //sheetNames.forEach{
        //    val sheet = Sheet().setProperties(SheetProperties().setTitle( it ) )
        //    sheetList.add( sheet )
        //}

        //val sheets = Spreadsheet().apply{
        //    properties = SpreadsheetProperties().setTitle("attendance")
        //    sheets= sheetList
        //}

        val requests = mutableListOf<Request>()
        sheetNames.forEach {
            requests.add( Request().setUpdateSpreadsheetProperties(
                UpdateSpreadsheetPropertiesRequest()
                    .setProperties(SpreadsheetProperties().setTitle(it))
                    .setFields("title")
            ))
        }

        val body = BatchUpdateSpreadsheetRequest().setRequests( requests )
        val res = service.spreadsheets().batchUpdate(sheet_id,body).execute()


        //val spreadsheet = service.spreadsheets().create( sheets ).execute()

        //Log.d("sheetid", spreadsheet.spreadsheetId )

        val sheetmap = mutableMapOf<String,MutableList<MutableList<Any>>>()
        sheetmap.put("student",createValueList().apply{
            this.add( mutableListOf("id","st_id","st_name","g_name","no"))
        })
        sheetmap.put("group",createValueList().apply{
            this.add(mutableListOf("id","g_name","mentor"))
        })
        sheetmap.put("attendance",createValueList().apply{
            this.add( mutableListOf("id","st_id","st_name","g_name","no","year","month",
                "daye","timed","sub_name","at_code"))
        })
        sheetmap.put("timetable",createValueList().apply{
            this.add(mutableListOf("id","g_name","day","timed","sub_name","sub_mentor"))
        })
        sheetmap.forEach{
            var result = service.spreadsheets().values()
                .append( sheet_id , "${it.key}!A1:K1",createBody( it.value ) )
                .setValueInputOption("RAW")

            result.execute()
        }
        //return spreadsheet.spreadsheetId
        return sheet_id
    }

    fun createValueList():MutableList<MutableList<Any>>{
        val list = mutableListOf<MutableList<Any>>()
        return list
    }
    fun createBody( value : MutableList<MutableList<Any>>):ValueRange{
        val valueRange = ValueRange().setValues(value)
        return valueRange
    }
}