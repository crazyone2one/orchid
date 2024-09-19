// uno.config.ts
import {
    defineConfig,
    presetAttributify,
    presetIcons,
    presetTypography,
    presetUno,
    presetWebFonts,
    transformerDirectives,
    transformerVariantGroup,
} from "unocss";
import {FileSystemIconLoader} from '@iconify/utils/lib/loader/node-loaders'
import fs from 'node:fs/promises'

export default defineConfig({
    shortcuts: [
        // ...
    ],
    theme: {
        colors: {
            // ...
        },
    },
    presets: [
        presetUno(),
        presetAttributify(),
        presetIcons({
            collections: {
                carbon: () => import("@iconify-json/carbon/icons.json").then((i) => i.default),
                ic: () => import("@iconify-json/ic/icons.json").then((i) => i.default),
                'o-icons': FileSystemIconLoader('./src/assets/icons', svg => svg.replace(/#fff/, 'currentColor')),
                'my-icons': {
                    success: () => fs.readFile('./src/assets/icons/success.svg', 'utf-8'),
                    'case-review': () => fs.readFile('./src/assets/icons/case-review.svg', 'utf-8'),
                },
                custom: {

                }
            },
            extraProperties: {
                'display': 'inline-block',
                'vertical-align': 'middle',
            }
        }),
        presetTypography(),
        presetWebFonts({
            fonts: {
                // ...
            },
        }),
    ],
    transformers: [transformerDirectives(), transformerVariantGroup()],
});
