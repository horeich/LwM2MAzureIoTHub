set(HERE ${PROJECT_SOURCE_DIR}/include)


set(WAKAAMA_HEADER_FILES ${HERE}/liblwm2m.h ${HERE}/connection.h ${HERE}/lightclient.h)
set(WAKAAMA_HEADER_FILES ${WAKAAMA_HEADER_FILES} PARENT_SCOPE)
source_group(include FILES WAKAAMA_HEADER_FILES)
