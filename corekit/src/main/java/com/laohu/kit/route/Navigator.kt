package com.laohu.kit.route

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.laohu.kit.BuildConfig
import com.laohu.kit.extensions.isNotNullOrEmpty
import com.laohu.kit.extensions.orFalse
import com.laohu.kit.extensions.requestActivityForResult
import com.laohu.kit.util.log.LHLogKit

typealias StartForResultHandler = (isResultOk: Boolean, data: Intent?) -> Unit

object Navigator {
    private lateinit var routeTable: RouteTable

    fun registerRouteTable(routeTable: RouteTable) {
        this.routeTable = routeTable
    }

    fun isCanNavigation(url: String): Boolean {
        verifyRouteTable()
        return isCanNavigation(url.toRouteUrl(routeTable.schemaHost))
    }

    fun isCanNavigation(uri: Uri): Boolean {
        verifyRouteTable()
        return routeTable.isRegistered(uri.getUriPath())
    }

    /**
     * @param   context                 当前context
     * @param   url                     要跳转的目标路由名,可以携带url参数
     * @param   params                  跳转到目标路由需要携带的参数
     * @param   startForResultHandler   是否需要采用startForResult的方式
     */
    fun navigation(
        context: Context?,
        url: String,
        params: Bundle? = null,
        startForResultHandler: StartForResultHandler? = null
    ) {
        if (context == null) {
            LHLogKit.e("navigation failure, because context is null")
            throw IllegalArgumentException("navigation failure, because context is null")
        }
        verifyRouteTable()
        LHLogKit.i("Route url: $url, params: ${params.toString()}")
        val routeUrl = RouteUrl(url = url, uri = Uri.parse(url.toRouteUrl(routeTable.schemaHost)))
        val currentSchemaHost = "${routeUrl.uri.scheme}://${routeUrl.uri.host}"
        if (routeTable.schemaHost.isNotNullOrEmpty() && currentSchemaHost != routeTable.schemaHost) {
            if (BuildConfig.DEBUG) {
                Toast.makeText(context, "Route host is not equals for '${routeTable.schemaHost}'", Toast.LENGTH_SHORT).show()
            }
            return
        }
        val originBundle = params ?: Bundle()
        routeUrl.uri.queryParameterNames.forEach {
            originBundle.putString(it, routeUrl.uri.getQueryParameter(it))
        }
        if (!routeTable.isRegistered(routeUrl.route)) {
            val param =
                routeTable.unknownRouteTarget?.parameterInterceptor?.invoke(routeUrl, originBundle) ?: originBundle
            navigationInternal(context, routeUrl, routeTable.unknownRouteTarget, param, startForResultHandler)
            return
        }
        val routeTarget = routeTable.getRouteTarget(routeUrl.route)!!.create()
        val param = routeTarget.parameterInterceptor?.invoke(routeUrl, originBundle) ?: originBundle
        navigationInternal(context, routeUrl, routeTarget, param, startForResultHandler)
    }

    private fun navigationInternal(
        context: Context,
        url: RouteUrl,
        routeTarget: RouteTarget?,
        param: Bundle,
        startForResultHandler: StartForResultHandler?
    ) {
        if (routeTarget == null) {
            LHLogKit.e("Unknown route")
            throw RuntimeException("Unknown route")
        }
        val isIntercept = routeTarget.navigationInterceptor?.invoke(context, url, param).orFalse()
        if (isIntercept) {
            LHLogKit.i("${url.uri} navigation is intercepted")
            return
        }
        when {
            routeTarget.clazz != null -> {
                val intent = buildRouteIntent(context, routeTarget, param)
                if (startForResultHandler != null && context is AppCompatActivity) {
                    context.requestActivityForResult(intent, startForResultHandler)
                } else {
                    context.startActivity(intent)
                }
            }
            routeTarget.navigation != null -> {
                routeTarget.navigation?.invoke(context, url, param)
            }
            else -> {
                LHLogKit.e("RouteTarget is invalid, $routeTarget")
                throw RuntimeException("RouteTarget is invalid, $routeTarget")
            }
        }
    }

    private fun buildRouteIntent(context: Context, routeTarget: RouteTarget, params: Bundle): Intent {
        return Intent(context, routeTarget.clazz).apply {
            putExtras(params)
        }
    }

    private fun verifyRouteTable() {
        if (!this::routeTable.isInitialized) {
            throw IllegalStateException("Please invoke method 'registerRouteTable' to init navigator")
        }
    }
}

private fun String.toRouteUrl(host: String?): String {
    if (host.isNullOrEmpty() || this.startsWith(host)) {
        return this
    }
    return "$host/$this"
}