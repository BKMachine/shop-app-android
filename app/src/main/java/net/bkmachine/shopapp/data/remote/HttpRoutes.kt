package net.bkmachine.shopapp.data.remote

object HttpRoutes {
    private const val BASE_URL = "https://app.bkmachine.net/api"
    const val PICK_TOOL = "$BASE_URL/tools/pick"
    const val TOOL_INFO = "$BASE_URL/tools/info/:scanCode"
}