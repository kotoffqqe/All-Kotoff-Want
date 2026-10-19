# All-Kotoff-Want

Client-side Fabric project targeting Minecraft Java 26.1.2. Requires Java 25 and the Fabric Loader/Fabric API versions listed in `gradle.properties`. The project uses Fabric Loom 1.18.4.

## Included tools
- **Item Workshop** (`R`): while the inventory is open, hover an inventory slot and press `R` to edit that exact stack; in the game view, `R` opens the editor for the main-hand item. The inventory screen is drawn behind the editor; open lore, enchantment and attribute editors. Item changes now use Minecraft's normal creative inventory sync path when the player is in Creative mode.
- **Lore Editor**: edit item lore, separating multiple lines with `|` because the current editor uses a single-line input field.
- **Enchantment Lab**: add/remove enchantments with levels up to 32767 (subject to version/game rules and server restrictions).
- **Attribute Lab**: apply a custom pair of main-hand attack damage/speed modifiers. This replaces the stack's current attribute-modifier component; use carefully.
- **Entity Lab**: generates and sends `/data merge entity` for the nearest matching entity, including custom name, No AI, Silent, Glowing, Invulnerable and No Gravity flags.
- **Passenger Builder**: summons a vehicle entity with one passenger.
- **Spawner Lab**: sends a `/data merge block` command to the block one block below the player, setting entity ID and common spawn timing/range values.

## Important prototype limitations
- In Creative mode, item edits are sent through the vanilla creative-slot synchronization path. In Survival/Adventure, edits are local-only and may be overwritten by server inventory updates. A client-only mod cannot force a vanilla server to accept arbitrary item components in those modes; server-side support or commands are required.
- Enchantment levels above vanilla limits are requested in the client item component, but their persistence and behavior depend on server acceptance and version rules.
- Entity/spawner command tools require permission and are not an NBT editor that bypasses server protections.

Entity, passenger and spawner tools use server commands. They require command permissions (for example, operator permissions in a private world/server). They do not bypass server protections. The entity tool affects the nearest matching entity, not necessarily the one under the crosshair. The spawner tool targets the block immediately below the player; stand on/above the intended spawner.

## Build
1. Install a JDK 25 distribution.
2. Install Gradle 9.7.1 (the version configured for this project).
3. From this directory, run `gradle build` (or `build.bat` on Windows / `./build.sh` on Linux and macOS).
4. The mod JAR should appear in `build/libs/` if the project compiles successfully.
5. A GitHub Actions workflow is included at `.github/workflows/build.yml`. After you upload the project to a GitHub repository, open the repository Actions tab and run **Build All-Kotoff-Want** (or push a commit). When the run succeeds, open it and download the artifact named `All-Kotoff-Want-<commit>`. Inside is the installable mod JAR. If the run fails, open the failed step and copy the complete error log so the source can be corrected.

The source ZIP intentionally does not bundle a Gradle wrapper JAR. This environment has only Java 21, no Gradle installation, and no network access, so I cannot run a real Gradle compile here. The build must be verified by GitHub Actions against Minecraft 26.1.2 before treating the new version as a tested release.

## Status
Version 0.3.2 fixes the decorative panel consuming mouse clicks, adds an animated accent sweep to the controls, and supports opening the editor over the inventory with the hovered slot as the target. It still needs a successful CI build and in-game testing. Test in a backup world before using it on a main save.


## Версия 0.3.4 — правки по обратной связи
- Интерфейс редакторов переведён на русский язык.
- Выбор зачарований сделан через циклический список с русскими названиями и пояснениями; уровень можно задавать до 32767.
- В настройке спавнера выбирается моб и изменяется именно блок спавнера, на который наведён прицел; если это не спавнер, показывается понятное сообщение. Для применения команды на сервере нужны права.
- Добавление/удаление бонусов атаки больше не должно заменять стандартные атрибуты предмета; удаление бонусов мода возвращает стандартные атрибуты, если пользовательских атрибутов не осталось.

Сборка всё ещё должна быть проверена GitHub Actions, а поведение в игре — протестировано на Minecraft 26.1.2.


## Version 0.3.4 — language selector

The main editor now includes a language button that cycles Russian → English → German. The selection applies to UI text throughout editor screens. Some less common descriptions may remain in Russian if a phrase is not yet included in the translation table.


## Версия 0.3.5 — прокрутка выбора
- Колесо мыши меняет выбранное зачарование, моба для спавнера, тип моба в редакторе сущностей и оба типа мобов в редакторе посадки.
- Прокрутка работает, когда курсор находится над соответствующим компактным селектором; текстовые поля и кнопки ниже не перехватывают колесо.
- Кнопки выбора оставлены как запасной способ переключения.

## Version 0.3.5 — mouse-wheel selection
- Mouse wheel changes the selected enchantment, spawner mob, entity type, and both passenger/vehicle types.
- Scroll over the compact selector to change the selection; other controls remain unaffected.

## v0.3.6
- Click-to-open compact picker panels with visible choices, wheel scrolling, and selected-item highlighting for enchantments and mob selectors.
- More restrained graphite/lavender UI with reduced visual noise.
- Item name and lore fields support inline formatting codes: `&0`–`&f` colors, `&#RRGGBB` custom hex colors, `&l` bold, `&o` italic, `&n` underline, `&m` strikethrough, `&k` obfuscated/magic text, and `&r` reset. Toolbar buttons insert the codes at the text cursor.
- Attribute editor adds a main-hand movement-speed modifier, active while the item is held. This is an item attribute modifier, not a potion status effect; arbitrary potion effects while holding would require server-side handling for reliable multiplayer behavior.


## v0.3.8
- The custom anime background was removed; editor tabs now use the previous inventory/game backdrop without the pasted-on art overlay.
- Added a reset action for custom name, lore, enchantments, stored enchantments, and attribute modifiers.

## v0.3.9
- Keeps editor drafts keyed to the selected inventory slot and item type instead of relying on Java object identity.
- Persists draft fields and selections across tabs for enchantments, attributes, spawner settings, mob settings, and passenger setup; the reset-all action clears these drafts.
- Enchantment editor reads and edits existing enchantment levels, including stored enchantments on enchanted books.
- Attribute editor preloads existing All-Kotoff-Want modifier values instead of always starting from defaults.
- Main editor displays the selected item's icon and current display name.
- Keeps the user's original text casing; italic formatting changes style only.
- Source archive only: compilation has not been verified in this environment.


## v0.4.0 changes
- Added HEX color editing for dyeable leather armor and reset support.
- Added editable item count up to 9,999,999; the stack max-size component is raised before setting count, so normally non-stackable items can be made into oversized stacks locally. Server acceptance depends on server validation.
- Added a combined compact name-and-lore editor tab.
- Reworked attribute editing into a picker covering 28 common combat, armor, movement, mining, interaction-range, health, gravity, and utility attributes. It shows the item's default value alongside its current value and removes only the selected All-Kotoff-Want override.
- Removed the numbered `/ 01`, `/ 02` etc. headings.
- Removed the custom background overlay and asset; the previous backdrop is restored.


## v0.4.1 changes
- Removed the pasted-on Death Note background and restored the prior screen backdrop.
- Added the combined Text & Lore tab.
- Increased the count editor range to 1–9,999,999, raising the stack's max-size component before applying the count.
- Replaced the three hard-coded attribute fields with a selectable attribute editor and default/current value readouts.
- Removed numbered page suffixes from headings.
- Source archive only; compilation and in-game behavior still require CI verification.


### v0.4.2
- Personal item library persists locally in `config/all-kotoff-want-items.json` across worlds and servers.
- Saved stacks can be deleted or sent to the currently selected inventory slot in Creative mode.
- Removed the spawner editor; mob settings now create a mob with chosen health, equipment/head item, name and potion effect.
- Oversized stacks get a unique hidden marker so separate dropped stacks do not merge into each other.


### v0.4.3
- Added a toggle for unbreakable items in the attribute editor.
- Rebuilt the saved-item library as a 9×3 inventory grid with working page navigation, one-click back, delete controls, and local hotbar assignment plus server sync.
- Added a dedicated multi-effect mob editor with per-effect enable toggles, level, and duration.
- Removed the animated offset from the mob editor so visible controls align with their click hitboxes.


### v0.4.4
- Библиотека: выбор предмета и отдельные кнопки «Выдать» / «Удалить» над ячейками.
- Количество предметов: увеличенные стаки получают индивидуальный идентификатор, а обычные количества возвращаются к стандартному размеру стака.
- Создание моба: выбор оружия и частей брони через список предметов с поиском по названию или ID и прокруткой; ручной ввод ID больше не нужен.
- Черновики редактора сохраняются в `config/all-kotoff-want-drafts.properties` между открытиями игры.
- Добавлены сбросы черновиков текущего раздела в редакторах мобов, эффектов, атрибутов, зачарований, текста, описания и пассажиров.

## 0.4.5 — TMI-style item picker
- Reworked armor and weapon selection into a compact creative-inventory-style icon grid.
- Added searchable item names and IDs, hover labels, mouse-wheel paging, and page/item counts.
- Kept selection connected to the saved mob-equipment drafts.


## v0.4.6 — Saved item library reliability
- Added explicit hotbar target selection (slots 1–9) before giving a saved item.
- Giving updates the local hotbar slot, sends the creative inventory slot update, and selects that slot.
- Save/delete operations now report persistence failures and roll back the in-memory change if writing the library file fails.
- Library loading waits for registry access when a saved library exists, avoiding a premature title-screen load.
- Preserved selected item/page while rebuilding the library screen.


## v0.4.7 — Localization, about screen and equipment slots
- Added Ukrainian, Polish and Czech to the in-editor language cycle alongside Russian, English and German. Added translated labels and picker hints for the main UI.
- Added an “About the mod” screen with a concise feature list and author credit: Dmitry Kotov (nekotoff).
- Split armor selection by equipment slot: helmets only for the head, chestplates/elytra for the chest, leggings for legs, and boots for feet.
- Rearranged the mob equipment buttons so armor is grouped together and weapon selection sits to the left of the effects button.
- Build still needs to be verified by GitHub Actions on Minecraft 26.1.2.


## v0.4.8 — Mob name styling
- Added a dedicated editor for mob nicknames with quick color, HEX color and formatting buttons.
- Added standard, uniform and enchanting-table style font choices.
- Nickname formatting is stored in the persistent draft and applied to the summon command as styled text JSON.
