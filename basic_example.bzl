"""
Provides a build rule for building the BasicExample from Blaze.
"""

load("//java/com/google/ads/interactivemedia/v3/samples:build_defs.bzl", "build_sample_package")

COMMON_DEPS = [
    "//third_party/java/android_libs/exoplayer:exoplayer2_core",
    "//third_party/java/android_libs/exoplayer:exoplayer2_dash",
    "//third_party/java/android_libs/exoplayer:exoplayer2_ext_ima",
    "//third_party/java/android_libs/exoplayer:exoplayer2_ext_mediasession",
    "//third_party/java/android_libs/exoplayer:exoplayer2_hls",
    "//third_party/java/android_libs/exoplayer:exoplayer2_ui",
    "//third_party/java/android_libs/guava_jdk5:collect",
    "//third_party/java/android/android_sdk_linux/extras/android/compatibility/fragment",
    "//third_party/java/androidx/annotation",
]

COMPILED_DEPS = COMMON_DEPS + [
    "//java/com/google/ads/interactivemedia/v3:sdk_lib",
    "//java/com/google/android/gmscore/integ/client/ads_identifier",
]

def basic_example_package():
    build_sample_package(
        name_prefix = "basic",
        package_name = "com.google.ads.interactivemedia.v3.samples.videoplayerapp",
        srcs = native.glob([
            "BasicExample/app/src/main/java/com/google/ads/interactivemedia/v3/samples/samplevideoplayer/*.java",
            "BasicExample/app/src/main/java/com/google/ads/interactivemedia/v3/samples/videoplayerapp/*.java",
        ]),
        compiled_deps = COMPILED_DEPS,
        debug_deps = COMMON_DEPS,
        manifest = "BasicExample/app/src/main/AndroidManifest.xml",
        resources = native.glob(["BasicExample/app/src/main/res/**"]),
    )
