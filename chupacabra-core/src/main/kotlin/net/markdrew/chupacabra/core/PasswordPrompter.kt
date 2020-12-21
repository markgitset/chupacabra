package net.markdrew.chupacabra.core

import java.awt.FlowLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.util.Arrays
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JPasswordField
import javax.swing.SwingUtilities
import javax.swing.WindowConstants

/**
 * Utility for prompting a user for a password in an environment that may or may not have a console. When a console is available,
 * the user is presented with a simple prompt on the console. When a console is NOT available (e.g., in Eclipse), the user is
 * shown a Swing window into which the password may be entered.
 *
 * Here's an example of its use:
 * <pre>
 * PasswordPrompter prompter = PasswordPrompter("'pass123'")
 *
 * // one way
 * prompter.promptUserForPassword()
 * if (!"pass123".equals(prompter.getPasswordString())) throw new Exception("Wrong password!")
 * prompter.clearPassword()
 * println("SUCCESS!")
 * println(prompter.getPasswordString())
 *
 * // another way
 * prompter.useAndClearPassword { p ->
 *     if (!"pass123".equals(new String(p))) throw new RuntimeException("Wrong password!")
 *     println("SUCCESS!")
 * }
 * println(prompter.getPasswordString())
 * </pre>
 */
class PasswordPrompter
/**
 * The user will be prompted to "Enter " + descriptionOfPassword + " password".
 *
 * @param descriptionOfPassword a description of the password, i.e. what's the password for?
 */
@JvmOverloads constructor(private val descriptionOfPassword: String? = null) {

    /**
     * Normally, just use [.promptUserForPassword], but if you need to re-retrieve a password for which the user was
     * already prompted (without prompting the user again), use this method.
     *
     * @return a password for which the user has already been prompted
     */
    var password: CharArray? = null
        private set

    /**
     * Prefer [.getPassword] because it can be cleared when done. If you must use this method, ensure you null out the
     * assigned reference when you're done using the password (if necessary) to make the reference eligible for garbage
     * collection.
     */
    val passwordString: String
        get() = String(password!!)

    /**
     * Zeros out the password for security reasons.
     */
    fun clearPassword() {
        if (password != null) Arrays.fill(password!!, '0')
    }

    /**
     * @param passwordConsumer consumer that uses the password
     */
    fun <T> useAndClearPassword(passwordConsumer: (password: CharArray) -> T): T = try {
        passwordConsumer(promptUserForPassword() ?: error("password is null!"))
    } finally {
        clearPassword()
    }

    /**
     * Prompts the user (one way or another) for a password. Stores and returns the user-provided password. If this
     * [PasswordPrompter] already contains a user-provided password, this method will clear it and replace it.
     */
    fun promptUserForPassword(): CharArray? {

        // if we already have a password, we're about to replace it, so go ahead and clear it for better security
        if (password != null) clearPassword()

        val desc = descriptionOfPassword ?: "the"
        
        val console = System.console()
        if (console != null) {

            // if a console's available, just use it
            password = console.readPassword("Enter $desc password: ")

        } else {
            try {

                // no console (e.g., running in Eclipse), so try the Swing approach
                val future = CompletableFuture<CharArray>()
                SwingUtilities.invokeLater { PasswordPrompterPanel.createAndShowGui(desc, future) }
                password = future.get()

            } catch (e: RuntimeException) {
                throw e
            } catch (e: ExecutionException) {
                throw RuntimeException(e.cause)
            } catch (e: InterruptedException) {
                throw RuntimeException(e)
            }

        }

        return password
    }

    internal class PasswordPrompterPanel(
        private val controllingFrame: JFrame, descOfPassword: String, private val future: CompletableFuture<CharArray>
    ) : JPanel(), ActionListener {

        private val passwordField: JPasswordField

        init {

            // Create everything
            passwordField = JPasswordField(20)
            passwordField.addActionListener(this)

            val label = JLabel("Enter $descOfPassword password: ")
            label.labelFor = passwordField
            val okButton = JButton("OK")
            okButton.addActionListener(this)

            // Lay out everything
            val textPane = JPanel(FlowLayout(FlowLayout.TRAILING))
            textPane.add(label)
            textPane.add(passwordField)

            add(textPane)
            add(okButton)
        }

        override fun actionPerformed(event: ActionEvent) {
            val input = passwordField.password
            future.complete(input)
            controllingFrame.dispose()
        }

        // Must be called from the event dispatch thread
        private fun resetFocus() {
            passwordField.requestFocusInWindow()
        }

        companion object {

            private val serialVersionUID = 1L

            /**
             * Create the GUI and show it.  For thread safety, this method should be invoked from the event dispatch thread.
             */
            fun createAndShowGui(descOfPassword: String, future: CompletableFuture<CharArray>) {//, PasswordPrompter2 prompter) {
                // Create and set up the window
                val frame = JFrame(descOfPassword)
                frame.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE

                // Create and set up the content pane
                val newContentPane = PasswordPrompterPanel(frame, descOfPassword, future)//, prompter);
                newContentPane.isOpaque = true // content panes must be opaque
                frame.contentPane = newContentPane

                // Make sure the focus goes to the right component whenever the frame is initially given the focus
                frame.addWindowListener(object : WindowAdapter() {
                    override fun windowActivated(e: WindowEvent?) {
                        newContentPane.resetFocus()
                    }
                })

                // Display the window
                frame.setLocationRelativeTo(null)
                frame.pack()
                frame.isVisible = true
            }
        }

    }

    companion object {

        /**
         * A simple, manual test.  Run in Eclipse for the GUI version.  Run on command line with something like
         * <pre>
         * java -cp CollectionProcessingService/CollectionCore/target/classes/ zymurgy.collection.core.utils.PasswordPrompter
         * </pre>
         * for the console version.
         */
        @Throws(Exception::class)
        @JvmStatic fun main(args: Array<String>) {
            val prompter = PasswordPrompter("'pass123'")

            // one way
            prompter.promptUserForPassword()
            if ("pass123" != prompter.passwordString) throw Exception("Wrong password!")
            prompter.clearPassword()
            println("SUCCESS!")
            println(prompter.passwordString)

            // another way
            prompter.useAndClearPassword { p ->
                if ("pass123" != String(p)) throw RuntimeException("Wrong password!")
                println("SUCCESS!")
            }
            println(prompter.passwordString)
        }
    }

}
