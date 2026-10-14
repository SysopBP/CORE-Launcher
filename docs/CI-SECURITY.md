# CI credential maintenance

The previously committed Gradle cache encryption key is public. Configure a new GRADLE_CACHE_ENCRYPTION_KEY secret before relying on encrypted configuration-cache retention. Remove affected old caches through GitHub Actions cache management. Changing the workflow cannot rotate an account secret or remove existing caches by itself.

Signing inputs are passed through environment variables to a Python writer that escapes Java properties. Temporary keystore/property files are removed even when later steps fail. Never include signing files in artifact/source patterns.

Actions are pinned to verified upstream commit IDs. Dependabot proposes updates; review action sources and build checks before accepting them. Configure required reviews/status checks for your actual default branch through repository settings; classic protection and account security are separate from these files.
