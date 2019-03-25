#include <stdio.h>
#include <stdlib.h>
#include <jni.h>
#include <util.h>
#include "instead.h"

static int tiny_init(void)
{
	int rc;
	rc = instead_loadfile("stead/tiny3.lua");
	if (rc)
		return rc;
	return 0;
}
static struct instead_ext ext = {
	.init = tiny_init,
};

JNIEXPORT jint JNICALL Java_org_emunix_metaparser_Game_registerExtension(JNIEnv* env, jclass cls) {
    return instead_extension(&ext);
}

JNIEXPORT jint JNICALL Java_org_emunix_metaparser_Game_insteadInit(JNIEnv* env, jclass cls, jstring directory, jstring gameDirectory) {
    const char *current_dir = (*env)->GetStringUTFChars(env, directory, NULL);
    const char *game_dir = (*env)->GetStringUTFChars(env, gameDirectory, NULL);
    setdir(current_dir);
    int ret = instead_init(game_dir);
    (*env)->ReleaseStringUTFChars(env, gameDirectory, game_dir);
    (*env)->ReleaseStringUTFChars(env, directory, current_dir);
    return ret;
}

JNIEXPORT jstring JNICALL Java_org_emunix_metaparser_Game_insteadCommand(JNIEnv* env, jclass cls, jstring command) {
    int rc;
    const char *cmd = (*env)->GetStringUTFChars(env, command, NULL);
    char* response = instead_cmd(cmd, &rc);
    jstring ret = (*env)->NewStringUTF(env, response);
    (*env)->ReleaseStringUTFChars(env, command, cmd);
    return ret;
}

JNIEXPORT jint JNICALL Java_org_emunix_metaparser_Game_insteadLoad(JNIEnv* env, jclass cls) {
    return instead_load(NULL);
}

JNIEXPORT void JNICALL Java_org_emunix_metaparser_Game_insteadDone(JNIEnv* env, jclass cls) {
    instead_done();
}