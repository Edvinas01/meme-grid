package com.edd.memegrid.util

class TemplateReader(private val enableCaching: Boolean, private val templateDir: String) {

    private companion object {
        const val EXTENSIONS_HTML = "html"
    }

    private val cache = mutableMapOf<String, String>()

    /**
     * Read HTML template and replace its variables.
     */
    fun read(name: String, variables: Map<String, Any> = emptyMap()): String {
        val html = readCachedTemplate("$name.$EXTENSIONS_HTML")
        if (variables.isEmpty()) {
            return html
        }

        return variables
                .entries
                .fold(html) { res, (name, value) ->
                    res.replace("\${$name}", value.toString())
                }
    }

    /**
     * Read cached template.
     */
    private fun readCachedTemplate(name: String): String {
        if (enableCaching) {
            val cached = cache[name]
            if (cached != null) {
                return cached
            }

            val read = readTemplate(name)
            synchronized(cache) {
                cache[name] = read
                return read
            }
        }
        return readTemplate(name)
    }

    /**
     * Read raw template.
     */
    private fun readTemplate(name: String): String {
        return TemplateReader::class.java
                .getResource("$templateDir/$name")
                .readText()
    }
}
