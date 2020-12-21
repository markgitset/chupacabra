package net.markdrew.chupacabra.cli

/**
 * Type for an application
 */
typealias App = (Array<String>) -> Unit

/**
 * Use a main function like this to build git-like CLI apps whose first argument is the name of a particular tool (i.e., app) to
 * use.  The arguments after the name of the tool are passed into the tool to handle.
 * 
 * <code>
 *     @JvmStatic
 *     fun main(args: Array<String>) {
 *         AppDemuxer(mapOf(
 *             "test1" to Test1::main,
 *             "test2" to Test2::main,
 *         )).run(args)
 *     }
 * </code>
 */
@Deprecated("Use Clikt subcommands instead")
class AppDemuxer(nameToApp: Map<String, App>) {
    
    // add the default help tool (if not already specified)
    private val appMap = 
        if (nameToApp.containsKey("help")) nameToApp
        else mapOf("help" to this::toolHelp) + nameToApp

    // handle an unknown tool name
    private fun unknownTool(toolName: String): App = {
        println("$toolName is not a tool name.")
        println("Tool name must be one of: ${appMap.keys}")
        println("For tool options, use 'help [tool-name]'")
    }
    
    private fun lookupApp(toolName: String): App = appMap[toolName] ?: unknownTool(toolName)

    // default help tool that runs tool named by the first argument with '--help' to let it print out its own help
    private fun toolHelp(args: Array<String>) {
        val toolName: String? = args.getOrNull(0)
        if (toolName == null || toolName in setOf("help", "--help")) {
            println("Available tool names: ${appMap.keys}")
            println("For tool options, use 'help [tool-name]'")
        } else {
            val app = lookupApp(toolName)
            app(arrayOf("--help"))
        }
    }

    fun run(args: Array<String>) {
        if (args.isEmpty() || args.size == 1 && args[0] in setOf("help", "--help")) {
            toolHelp(emptyArray())
        } else {
            val app: App = lookupApp(args[0])
            app(args.drop(1).toTypedArray())
        }
    }

}
