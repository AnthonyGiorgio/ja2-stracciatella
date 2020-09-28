package io.github.ja2stracciatella

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import io.github.ja2stracciatella.ui.main.SectionsPagerAdapter
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import java.io.File
import java.io.IOException


class LauncherActivity : AppCompatActivity() {
    private val activityLogTag = "LauncherActivity"
    private val jsonFormat = Json {
        prettyPrint = true
    }
    private val ja2JsonFilename = "ja2.json"
    private val gameDirKey = "game_dir"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadJA2Json()

        setContentView(R.layout.activity_launcher)
        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)
        val fab: FloatingActionButton = findViewById(R.id.fab)

        fab.setOnClickListener { _ ->
            try {
                saveJA2Json()
                val intent = Intent(this@LauncherActivity, StracciatellaActivity::class.java)
                startActivity(intent)
            } catch (e: IOException) {
                val message = "Could not write ${ja2JsonPath}: ${e.message}"
                Log.e(activityLogTag, message)
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private val ja2JsonPath: String
        get() {
            return "${applicationContext.filesDir.absolutePath}/$ja2JsonFilename"
        }

    private fun loadJA2Json() {
        val configurationModel = ViewModelProvider(this).get(ConfigurationModel::class.java)
        try {
            val json = File(ja2JsonPath).readText();
            // For some reason it is not possible to decode to Any, so we decode to JsonElement instead
            val jsonMap: Map<String, JsonElement> = jsonFormat.decodeFromString(json);

            Log.i(activityLogTag, "Loaded ja2.json: $jsonMap")

            val vanillaGameDir = jsonMap[gameDirKey];
            if (vanillaGameDir is JsonPrimitive && vanillaGameDir.isString) {
                configurationModel.setVanillaGameDir(vanillaGameDir.content);
            } else {
                throw SerializationException("$gameDirKey is not a string")
            }
        } catch (e: SerializationException) {
            Log.w(activityLogTag, "Could not decode ja2.json: ${e.message}")
        } catch (e: IOException) {
            Log.w(activityLogTag, "Could not read $ja2JsonPath: ${e.message}")
        }
    }

    private fun saveJA2Json() {
        val configurationModel = ViewModelProvider(this).get(ConfigurationModel::class.java)
        var jsonMap: MutableMap<String, JsonElement> = mutableMapOf()
        try {
            val json = File(ja2JsonPath).readText();
            // For some reason it is not possible to decode to Any, so we decode to JsonElement instead
            jsonMap = jsonFormat.decodeFromString(json)
        } catch (e: SerializationException) {
            Log.w(activityLogTag, "Could not decode ja2.json: ${e.message}")
        } catch (e: IOException) {
            Log.w(activityLogTag, "Could not read ${ja2JsonPath}: ${e.message}")
        }
        jsonMap["game_dir"] = JsonPrimitive(configurationModel.vanillaGameDir.value)
        Log.i(activityLogTag, "Starting with ja2.json: $jsonMap")
        File(ja2JsonPath).writeText(jsonFormat.encodeToString(jsonMap))
    }
}