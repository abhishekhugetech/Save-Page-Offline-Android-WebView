package com.abhishek.savepageoffline

import android.annotation.SuppressLint
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_saved_pages.*
import java.io.File

class SavedPages : AppCompatActivity() {

    lateinit var fileProcessor : LoadFilesFromDisk
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_pages)
        setUpViews()
    }

    private fun setUpViews() {
        goBack.setOnClickListener { onBackPressed() }
    }

    override fun onStart() {
        super.onStart()
        fileProcessor = LoadFilesFromDisk()
        fileProcessor.execute()
    }

    override fun onStop() {
        super.onStop()
        fileProcessor.cancel(true)
    }

    @SuppressLint("StaticFieldLeak")
    inner class LoadFilesFromDisk : AsyncTask<Void,Int,List<File>>(){
        override fun onPreExecute() {
            super.onPreExecute()
            progressLayout.visibility = View.VISIBLE
        }

        override fun onPostExecute(result: List<File>) {
            super.onPostExecute(result)
            progressLayout.visibility = View.GONE
            setUpFileList(result)
        }

        override fun doInBackground(vararg params: Void?): List<File> {
            val savedPagesFolder = File(filesDir.absolutePath  )
            val fileList = savedPagesFolder.listFiles()
            return fileList?.toList() ?: emptyList()
        }


    }

    private fun setUpFileList(result: List<File>) {
        val fileList = mutableListOf<String>()
        result.forEach { fileList.add(it.name.substring(0,it.name.lastIndexOf(".mhtml"))) }
        filesList.adapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,fileList)
        filesList.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val intent = Intent()
                intent.putExtra( MainActivity.FILE_DATA , result[position].path)
                setResult( MainActivity.FILE_OPEN_REQUEST , intent )
                finish()
            }
    }
}
