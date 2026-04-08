package net.bkmachine.shopapp.data.remote

object HttpRoutes {
    private const val BASE_URL = "https://app.bkmachine.net/api"
    const val BASE_IMAGE_URL = "https://app.bkmachine.net"
    
    // Tools
    const val PICK_TOOL = "$BASE_URL/tools/pick"
    const val TOOL_INFO = "$BASE_URL/tools/info/:scanCode"
    const val TOOL_STOCK = "$BASE_URL/tools/stock"
    
    // Parts
    const val PART_INFO = "$BASE_URL/parts/info/:scanCode"
    const val PART_STOCK = "$BASE_URL/parts/stock"
    
    // Images
    const val IMAGE_UPLOAD = "$BASE_URL/images/uploads/file"
    const val RECENT_UPLOADS = "$BASE_URL/images/uploads/recent"
    
    // Common
    const val REGISTER_DEVICE = "$BASE_URL/devices/register"
    const val GET_DEVICE_ME = "$BASE_URL/devices/me"
}
