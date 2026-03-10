package com.wbrawner.simplemarkdown.utility

import android.database.Cursor
import android.database.MatrixCursor
import android.os.CancellationSignal
import android.os.Environment.DIRECTORY_DOCUMENTS
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import android.provider.DocumentsProvider
import com.wbrawner.simplemarkdown.R
import timber.log.Timber
import java.io.File


private const val ROOT_ID = "root"

private const val ROOT_DOCUMENT_ID = "/"

class AutosaveDocumentsProvider : DocumentsProvider() {
    private val authority: String
        get() = context?.packageName.orEmpty() + ".autosavedocumentprovider"

    override fun onCreate(): Boolean = true

    override fun openDocument(
        documentId: String,
        mode: String,
        signal: CancellationSignal?
    ): ParcelFileDescriptor? {
        Timber.d("openDocument $documentId $mode $signal")
        val file = getFileForDoc(documentId)
        val accessMode = ParcelFileDescriptor.parseMode(mode)
        return ParcelFileDescriptor.open(file, accessMode)
    }

    // TODO: Check sort order
    override fun queryChildDocuments(
        parentDocumentId: String?,
        projection: Array<out String?>?,
        sortOrder: String?
    ): Cursor = MatrixCursor(projection ?: DEFAULT_DOCUMENT_PROJECTION).apply {
        Timber.d("queryChildDocuments $parentDocumentId $projection $sortOrder")
        val parent = getFileForDoc(parentDocumentId)
        parent.listFiles()
            .orEmpty()
            .forEach {
                includeFile(it)
            }
    }

    override fun queryDocument(
        documentId: String?,
        projection: Array<out String?>?
    ): Cursor = MatrixCursor(projection ?: DEFAULT_DOCUMENT_PROJECTION).apply {
        Timber.d("queryDocument $documentId $projection")
        val file = getFileForDoc(documentId)
        if (file.exists()) {
            includeFile(
                file,
                canDelete = documentId != ROOT_DOCUMENT_ID && documentId != DIRECTORY_DOCUMENTS
            )
        }
    }

    override fun queryRoots(projection: Array<out String?>?): Cursor =
        MatrixCursor(projection ?: DEFAULT_ROOT_PROJECTION).apply {
            Timber.d("queryRoots $projection")
            val row = newRow()
            row.add(DocumentsContract.Root.COLUMN_ROOT_ID, ROOT_ID)
            row.add(DocumentsContract.Root.COLUMN_ICON, R.mipmap.ic_launcher)
            row.add(DocumentsContract.Root.COLUMN_TITLE, context?.getString(R.string.app_name))
            row.add(
                DocumentsContract.Root.COLUMN_FLAGS,
                DocumentsContract.Root.FLAG_LOCAL_ONLY or DocumentsContract.Root.FLAG_SUPPORTS_CREATE
            )
            row.add(DocumentsContract.Root.COLUMN_DOCUMENT_ID, ROOT_DOCUMENT_ID)
        }

    // TODO: Handle directory creation
    override fun createDocument(
        parentDocumentId: String,
        mimeType: String,
        displayName: String
    ): String? {
        Timber.d("createDocument $parentDocumentId $mimeType $displayName")
        val parent = getFileForDoc(parentDocumentId)
        return File(parent, displayName).apply {
            createNewFile()
            setReadable(true)
            setWritable(true)
        }.name
    }

    // TODO: This doesn't update the UI after deleting a file, investigate and fix
    override fun deleteDocument(documentId: String) {
        Timber.d("deleteDocument $documentId")
        if (!getFileForDoc(documentId).delete()) {
            Timber.w("Failed to delete $documentId")
        } else {
            context?.contentResolver?.notifyChange(
                DocumentsContract.buildDocumentUri(
                    authority,
                    documentId
                ), null
            )
                ?: Timber.w("failed to notify change, contentResolver was null")
        }
    }

    private val internalStorageDir by lazy {
        context?.getExternalFilesDir(DIRECTORY_DOCUMENTS)
            ?: context?.filesDir
            ?: error("failed to retrieve context")
    }

    // TODO: Probably need the inverse of this function as well
    // TODO: Double check handling of files within subdirectories
    private fun getFileForDoc(documentId: String?): File {
        Timber.d("getFileForDoc $documentId")
        if (documentId == ROOT_DOCUMENT_ID || documentId == DIRECTORY_DOCUMENTS) {
            return internalStorageDir
        }

        return File(internalStorageDir, documentId.orEmpty())
    }

    private fun MatrixCursor.includeFile(
        file: File,
        canDelete: Boolean = true,
        canCreateFiles: Boolean = file.isDirectory
    ) = newRow().apply {
        add(DocumentsContract.Document.COLUMN_DOCUMENT_ID, file.name)
        add(
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            if (file.isDirectory) DocumentsContract.Document.MIME_TYPE_DIR else "text/markdown"
        )
        add(DocumentsContract.Document.COLUMN_DISPLAY_NAME, file.name)
        add(DocumentsContract.Document.COLUMN_LAST_MODIFIED, file.lastModified())
        var flags = DocumentsContract.Document.FLAG_SUPPORTS_WRITE
        /**
         * TODO: Fix the issues with creation and deletion so these features can be supported
        if (file.isDirectory && canCreateFiles) {
        flags = flags or DocumentsContract.Document.FLAG_DIR_SUPPORTS_CREATE
        }
        if (canDelete) {
        flags = flags or DocumentsContract.Document.FLAG_SUPPORTS_DELETE
        }
         */
        add(DocumentsContract.Document.COLUMN_FLAGS, flags)
        add(DocumentsContract.Document.COLUMN_SIZE, file.length())
    }

    companion object {
        private val DEFAULT_ROOT_PROJECTION = arrayOf(
            DocumentsContract.Root.COLUMN_ROOT_ID,
            DocumentsContract.Root.COLUMN_ICON,
            DocumentsContract.Root.COLUMN_TITLE,
            DocumentsContract.Root.COLUMN_FLAGS,
            DocumentsContract.Root.COLUMN_DOCUMENT_ID
        )

        private val DEFAULT_DOCUMENT_PROJECTION: Array<String> = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED,
            DocumentsContract.Document.COLUMN_FLAGS,
            DocumentsContract.Document.COLUMN_SIZE
        )
    }
}