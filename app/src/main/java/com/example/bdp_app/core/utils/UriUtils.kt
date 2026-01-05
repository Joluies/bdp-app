package com.example.bdp_app.core.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object UriUtils {
    fun fileFromContentUri(context: Context, contentUri: Uri): File {
        val fileExtension = getFileExtension(context, contentUri)
        val fileName = "temp_file_" + System.currentTimeMillis() + if (fileExtension != null) ".$fileExtension" else ""

        val tempFile = File(context.cacheDir, fileName)
        tempFile.createNewFile()

        try {
            val oStream = FileOutputStream(tempFile)
            val inputStream: InputStream? = context.contentResolver.openInputStream(contentUri)

            inputStream?.let {
                copy(inputStream, oStream)
            }
            oStream.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return tempFile
    }

    private fun getFileExtension(context: Context, uri: Uri): String? {
        val fileType: String? = context.contentResolver.getType(uri)
        return android.webkit.MimeTypeMap.getSingleton().getExtensionFromMimeType(fileType)
    }

    private fun copy(source: InputStream, target: FileOutputStream) {
        val buf = ByteArray(8192)
        var length: Int
        while (source.read(buf).also { length = it } > 0) {
            target.write(buf, 0, length)
        }
    }
}