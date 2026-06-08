# Security Policy

## Supported Versions

Only the latest published release is currently supported for security fixes.

Pre-release versions such as `0.1.0-alpha01` are experimental and may change without backward compatibility guarantees.

## Reporting a Vulnerability

Do not open a public issue for security-sensitive reports.

Report vulnerabilities privately by contacting the maintainer through GitHub:

- Repository: https://github.com/manoj-mili/readability-kmp
- Maintainer: https://github.com/manoj-mili

Please include:

- A clear description of the issue.
- Steps to reproduce.
- A minimal proof of concept if available.
- Affected version or commit.

## Scope

This SDK bundles Mozilla Readability and helps host apps inject it into browser contexts. Host apps remain responsible for safe WebView/WKWebView configuration, navigation policy, JavaScript execution policy, and handling untrusted page content.
