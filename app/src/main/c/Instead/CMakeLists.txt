set(INSTEAD_SRC_DIR Instead/src)

include_directories( ${INSTEAD_SRC_DIR}/instead ../Lua/Lua/src )

set( SOURCES
        "metaparser.c"
        "${INSTEAD_SRC_DIR}/instead/cache.c"
        "${INSTEAD_SRC_DIR}/instead/idf.c"
        "${INSTEAD_SRC_DIR}/instead/instead.c"
        "${INSTEAD_SRC_DIR}/instead/lfs.c"
        "${INSTEAD_SRC_DIR}/instead/list.c"
        "${INSTEAD_SRC_DIR}/instead/snprintf.c"
        "${INSTEAD_SRC_DIR}/instead/tinymt32.c"
        "${INSTEAD_SRC_DIR}/instead/util.c" )

add_definitions( -DANDROID -DVERSION=\"3.3.0\" )

add_library( metaparser SHARED ${SOURCES} )

find_library( log-lib log )

target_link_libraries( metaparser
                       lua
                        ${log-lib} )