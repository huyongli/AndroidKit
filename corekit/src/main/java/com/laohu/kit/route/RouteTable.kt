package com.laohu.kit.route

import android.content.Context
import android.net.Uri
import android.os.Bundle

class CommonRouteTargetFactory(private val routeTarget: RouteTarget) : RouteTargetFactory {
    override fun create(): RouteTarget {
        return routeTarget
    }
}

fun RouteTarget.toFactory(): RouteTargetFactory {
    return CommonRouteTargetFactory(this)
}

/**
 * 路由对象
 * @param   clazz                   路由页面Activity对应的class
 * @param   navigation              不通过class对象来进行路由跳转，采用自定义的跳转方式通过此方法参数自行实现，此时clazz参数必须为空
 * @param   parameterInterceptor    参数处理拦截器，对参数进行一定的加工处理
 * @param   navigationInterceptor   开始路由跳转之前，可以拦截做一些事情，比如判断当前是否已登录，如果未登录则不跳转，返回true则会拦截路由不跳转
 */
open class RouteTarget(
    open val clazz: Class<*>? = null,
    open val navigation: ((Context, RouteUrl, Bundle) -> Boolean)? = null,
    open val parameterInterceptor: ((RouteUrl, Bundle) -> Bundle)? = null,
    open val navigationInterceptor: ((Context, RouteUrl, Bundle) -> Boolean)? = null
)

/**
 * 路由对象工厂
 */
interface RouteTargetFactory {
    fun create(): RouteTarget
}

data class RouteUrl(
    val url: String,
    val uri: Uri
) {
    val route: String
        get() = uri.getUriPath()
}

/**
 * 路由注册表
 */
class RouteTable {
    private val table = mutableMapOf<String, RouteTargetFactory>()
    private var host: String? = null
    val schemaHost: String?
        get() = host
    private var unknownRoute: RouteTarget? = null
    val unknownRouteTarget: RouteTarget?
        get() = unknownRoute

    /**
     * @param   routeTable          要注册的路由表
     * @param   schemaHost          schema格式路由的host
     * @param   unknownRouteTarget  未注册的无法识别的路由的跳转方式
     */
    fun register(
        vararg routeTable: Map<String, RouteTargetFactory>,
        schemaHost: String? = null,
        unknownRouteTarget: RouteTargetFactory? = null
    ) {
        routeTable.forEach {
            this.table.putAll(it)
        }
        this.unknownRoute = unknownRouteTarget?.create()
        this.host = schemaHost
    }

    fun isRegistered(route: String) = table.containsKey(route)

    fun getRouteTarget(route: String) = table[route]
}

fun Uri?.getUriPath(): String {
    val path = this?.path ?: return ""
    if (path.isEmpty()) return ""
    if (path.startsWith("/")) {
        return path.substring(1)
    }
    return path
}