package org.junit.rules

import java.io.File
import java.io.IOException

import org.junit.Rule

/**
 * The TemporaryFolder Rule allows creation of files and folders that should
 * be deleted when the test method finishes (whether it passes or
 * fails). Whether the deletion is successful or not is not checked by this rule.
 * No exception will be thrown in case the deletion fails.

 *
 * Example of usage:
 *
 * public static class HasTempFolder {
 * &#064;Rule
 * public TemporaryFolder folder= new TemporaryFolder();

 * &#064;Test
 * public void testUsingTempFolder() throws IOException {
 * File createdFile= folder.newFile(&quot;myfile.txt&quot;);
 * File createdFolder= folder.newFolder(&quot;subfolder&quot;);
 * // ...
 * }
 * }
 *

 * @since 4.7
 */
class TemporaryFolder @JvmOverloads constructor(private val parentFolder: File? = null) : ExternalResource() {
    private var folder: File? = null

    @Throws(Throwable::class)
    override fun before() {
        create()
    }

    override fun after() {
        delete()
    }

    // testing purposes only

    /**
     * for testing purposes only. Do not use.
     */
    @Throws(IOException::class)
    fun create() {
        folder = createTemporaryFolderIn(parentFolder)
    }

    /**
     * Returns a new fresh file with the given name under the temporary folder.
     */
    @Throws(IOException::class)
    fun newFile(fileName: String): File {
        val file = File(root, fileName)
        if (!file.createNewFile()) {
            throw IOException(
                    "a file with the name \'$fileName\' already exists in the test folder")
        }
        return file
    }

    /**
     * Returns a new fresh file with a random name under the temporary folder.
     */
    @Throws(IOException::class)
    fun newFile(): File {
        return File.createTempFile("junit", null, root)
    }

    /**
     * Returns a new fresh folder with the given name under the temporary
     * folder.
     */
    @Throws(IOException::class)
    fun newFolder(folder: String): File {
        return newFolder(*arrayOf(folder))
    }

    /**
     * Returns a new fresh folder with the given name(s) under the temporary
     * folder.
     */
    @Throws(IOException::class)
    fun newFolder(vararg folderNames: String): File {
        var file = root
        for (i in folderNames.indices) {
            val folderName = folderNames[i]
            validateFolderName(folderName)
            file = File(file, folderName)
            if (!file.mkdir() && isLastElementInArray(i, folderNames)) {
                throw IOException(
                        "a folder with the name \'$folderName\' already exists")
            }
        }
        return file
    }

    /**
     * Validates if multiple path components were used while creating a folder.

     * @param folderName
     * *            Name of the folder being created
     */
    @Throws(IOException::class)
    private fun validateFolderName(folderName: String) {
        val tempFile = File(folderName)
        if (tempFile.parent != null) {
            val errorMsg = "Folder name cannot consist of multiple path components separated by a file separator." + " Please use newFolder('MyParentFolder','MyFolder') to create hierarchies of folders"
            throw IOException(errorMsg)
        }
    }

    private fun isLastElementInArray(index: Int, array: Array<String>): Boolean {
        return index == array.size - 1
    }

    /**
     * Returns a new fresh folder with a random name under the temporary folder.
     */
    @Throws(IOException::class)
    fun newFolder(): File {
        return createTemporaryFolderIn(root)
    }

    @Throws(IOException::class)
    private fun createTemporaryFolderIn(parentFolder: File): File {
        val createdFolder = File.createTempFile("junit", "", parentFolder)
        createdFolder.delete()
        createdFolder.mkdir()
        return createdFolder
    }

    /**
     * @return the location of this temporary folder.
     */
    val root: File
        get() {
            if (folder == null) {
                throw IllegalStateException(
                        "the temporary folder has not yet been created")
            }
            return folder
        }

    /**
     * Delete all files and folders under the temporary folder. Usually not
     * called directly, since it is automatically applied by the [Rule]
     */
    fun delete() {
        if (folder != null) {
            recursiveDelete(folder)
        }
    }

    private fun recursiveDelete(file: File) {
        val files = file.listFiles()
        if (files != null) {
            for (each in files) {
                recursiveDelete(each)
            }
        }
        file.delete()
    }
}
