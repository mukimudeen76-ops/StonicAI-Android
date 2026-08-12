package com.stonicai.app.ui.components

import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.linkify.LinkifyPlugin
import io.noties.markwon.syntax.Prism4jThemeDarkula
import io.noties.markwon.syntax.SyntaxHighlightPlugin
import io.noties.prism4j.Prism4j
import io.noties.prism4j.annotations.PrismBundle

@PrismBundle(includeAll = true)
class Prism4jGrammarLocator

@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    linkColor: Color = Color(0xFF00E5FF)
) {
    val context = LocalContext.current
    val markwon = remember {
        val prism4j = Prism4j(Prism4jGrammarLocator())
        Markwon.builder(context)
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TablePlugin.create(context))
            .usePlugin(LinkifyPlugin.create())
            .usePlugin(
                SyntaxHighlightPlugin.create(
                    prism4j,
                    Prism4jThemeDarkula.create()
                )
            )
            .build()
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            TextView(ctx).apply {
                setTextIsSelectable(true)
                setTextColor(textColor.toArgb())
                setLinkTextColor(linkColor.toArgb())
                textSize = 14f
                movementMethod = android.text.method.LinkMovementMethod.getInstance()
            }
        },
        update = { tv ->
            tv.setTextColor(textColor.toArgb())
            markwon.setMarkdown(tv, markdown.ifBlank { "…" })
        }
    )
}
