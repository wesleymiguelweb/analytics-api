$ErrorActionPreference = "Stop"
$baseDir = "D:\Projetos\analytics-api\src\main\java\com\growthmachine\analytics"

# Create new directory structure
$dirs = @(
    "$baseDir\domain",
    "$baseDir\application",
    "$baseDir\infrastructure\adapter\in\web",
    "$baseDir\infrastructure\adapter\out\persistence"
)
foreach ($dir in $dirs) {
    if (-not (Test-Path $dir)) {
        New-Item -Path $dir -ItemType Directory -Force | Out-Null
    }
}

# Move directories
$moves = @(
    @{ Src = "$baseDir\model"; Dest = "$baseDir\domain" },
    @{ Src = "$baseDir\exception"; Dest = "$baseDir\domain" },
    @{ Src = "$baseDir\service"; Dest = "$baseDir\application" },
    @{ Src = "$baseDir\controller"; Dest = "$baseDir\infrastructure\adapter\in\web" },
    @{ Src = "$baseDir\repository"; Dest = "$baseDir\infrastructure\adapter\out\persistence" },
    @{ Src = "$baseDir\config"; Dest = "$baseDir\infrastructure" }
)

foreach ($move in $moves) {
    if (Test-Path $move.Src) {
        Move-Item -Path $move.Src -Destination $move.Dest -Force
    }
}

# Define replacements
$replacements = @{
    "package com.growthmachine.analytics.model" = "package com.growthmachine.analytics.domain.model"
    "import com.growthmachine.analytics.model" = "import com.growthmachine.analytics.domain.model"
    
    "package com.growthmachine.analytics.exception" = "package com.growthmachine.analytics.domain.exception"
    "import com.growthmachine.analytics.exception" = "import com.growthmachine.analytics.domain.exception"
    
    "package com.growthmachine.analytics.service" = "package com.growthmachine.analytics.application.service"
    "import com.growthmachine.analytics.service" = "import com.growthmachine.analytics.application.service"
    
    "package com.growthmachine.analytics.controller" = "package com.growthmachine.analytics.infrastructure.adapter.in.web.controller"
    "import com.growthmachine.analytics.controller" = "import com.growthmachine.analytics.infrastructure.adapter.in.web.controller"
    
    "package com.growthmachine.analytics.repository" = "package com.growthmachine.analytics.infrastructure.adapter.out.persistence.repository"
    "import com.growthmachine.analytics.repository" = "import com.growthmachine.analytics.infrastructure.adapter.out.persistence.repository"
    
    "package com.growthmachine.analytics.config" = "package com.growthmachine.analytics.infrastructure.config"
    "import com.growthmachine.analytics.config" = "import com.growthmachine.analytics.infrastructure.config"
}

# Recursively update files
$files = Get-ChildItem -Path $baseDir -Filter *.java -Recurse

foreach ($file in $files) {
    $content = Get-Content $file.FullName -Raw
    $modified = $false
    
    foreach ($key in $replacements.Keys) {
        if ($content -match [regex]::Escape($key)) {
            $content = $content -replace [regex]::Escape($key), $replacements[$key]
            $modified = $true
        }
    }
    
    if ($modified) {
        Set-Content -Path $file.FullName -Value $content -NoNewline
    }
}

Write-Output "Refactoring completed successfully."
