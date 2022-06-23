package main

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"log"
	"path"
	"strings"
)

type Tags struct {
	Replace bool `json:"replace"`
	Values []string `json:"values"`
}

func main() {
	lang := "en_us"
	translations := readLanguageMap(lang)

	ores := []string{
		"copper",
		"gold",
		"iron",
	}

	trees := []string{
		"oak",
	}

	blockSaplingTags := Tags{}
	itemSaplingTags := Tags{}

	for _, tree := range trees {
		blockLogTags := Tags{}
		itemLogTags := Tags{}

		for _, ore := range ores {
			log.Printf("Generating %s %s", ore, tree)

			generateLog(ore, tree)
			translations[fmt.Sprintf("block.iron_oak.%[1]s_%[2]s_log", ore, tree)] = strings.Title(fmt.Sprintf("%s infused %s log", ore, tree))

			generateSapling(ore, tree)
			translations[fmt.Sprintf("block.iron_oak.%[1]s_%[2]s_sapling", ore, tree)] = strings.Title(fmt.Sprintf("%s infused %s sapling", ore, tree))

			blockLogTags.Values = append(blockLogTags.Values, fmt.Sprintf("iron_oak:%[1]s_%[2]s_log", ore, tree))
			itemLogTags.Values = append(itemLogTags.Values, fmt.Sprintf("iron_oak:%[1]s_%[2]s_log", ore, tree))

			blockSaplingTags.Values = append(blockSaplingTags.Values, fmt.Sprintf("iron_oak:%[1]s_%[2]s_sapling", ore, tree))
			itemSaplingTags.Values = append(itemSaplingTags.Values, fmt.Sprintf("iron_oak:%[1]s_%[2]s_sapling", ore, tree))
		}

		log.Printf("Generating tags for %s", tree)

		makeJsonFile(fmt.Sprintf("data/minecraft/tags/blocks/%s_logs.json", tree), blockLogTags)
		makeJsonFile(fmt.Sprintf("data/minecraft/tags/items/%s_logs.json", tree), itemLogTags)
	}

	log.Printf("Generating global tags")

	makeJsonFile(fmt.Sprintf("data/minecraft/tags/blocks/saplings.json"), blockSaplingTags)
	makeJsonFile(fmt.Sprintf("data/minecraft/tags/items/saplings.json"), itemSaplingTags)

	makeJsonFile( fmt.Sprintf("assets/iron_oak/lang/%s.json", lang), translations)
}

// Generates everything required for a log
func generateLog(ore, tree string) {
	// Block State
	makeFile(fmt.Sprintf("assets/iron_oak/blockstates/%[1]s_%[2]s_log.json", ore, tree), fmt.Sprintf(`{
  "variants": {
    "axis=x": {
      "model": "iron_oak:block/%[1]s_%[2]s_log_horizontal",
      "x": 90,
      "y": 90
    },
    "axis=y": {
      "model": "iron_oak:block/%[1]s_%[2]s_log"
    },
    "axis=z": {
      "model": "iron_oak:block/%[1]s_%[2]s_log_horizontal",
      "x": 90
    }
  }
}`, ore, tree))

	// Log Model
	makeFile(fmt.Sprintf("assets/iron_oak/models/block/%[1]s_%[2]s_log.json", ore, tree), fmt.Sprintf(`{
  "parent": "minecraft:block/cube_column",
  "textures": {
    "end": "minecraft:block/%[2]s_log_top",
    "side": "iron_oak:block/%[1]s_%[2]s_log"
  }
}`, ore, tree))

	// Horizontal Log Model
	makeFile(fmt.Sprintf("assets/iron_oak/models/block/%[1]s_%[2]s_log_horizontal.json", ore, tree), fmt.Sprintf(`{
  "parent": "minecraft:block/cube_column_horizontal",
  "textures": {
    "end": "minecraft:block/%[2]s_log_top",
    "side": "iron_oak:block/%[1]s_%[2]s_log"
  }
}`, ore, tree))

	// Log Item
	makeFile(fmt.Sprintf("assets/iron_oak/models/item/%[1]s_%[2]s_log.json", ore, tree), fmt.Sprintf(`{
  "parent": "iron_oak:block/%[1]s_%[2]s_log"
}`, ore, tree))

	// Textures
	// TODO: Merge Textures

	// Loot Table
	makeFile(fmt.Sprintf("data/iron_oak/loot_tables/blocks/%[1]s_%[2]s_log.json", ore, tree), fmt.Sprintf(`{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1.0,
      "bonus_rolls": 0.0,
      "entries": [
        {
          "type": "minecraft:item",
          "name": "iron_oak:%[1]s_%[2]s_log"
        }
      ],
      "conditions": [
        {
          "condition": "minecraft:survives_explosion"
        }
      ]
    }
  ]
}`, ore, tree))
}

func generateSapling(ore, tree string) {
	// Block State
	makeFile(fmt.Sprintf("assets/iron_oak/blockstates/%[1]s_%[2]s_sapling.json", ore, tree), fmt.Sprintf(`{
  "variants": {
    "": {
      "model": "iron_oak:block/%[1]s_%[2]s_sapling"
    }
  }
}`, ore, tree))

	// Model
	makeFile(fmt.Sprintf("assets/iron_oak/models/block/%[1]s_%[2]s_sapling.json", ore, tree), fmt.Sprintf(`{
  "parent": "minecraft:block/cross",
  "textures": {
    "cross": "iron_oak:block/%[1]s_%[2]s_sapling"
  }
}`, ore, tree))

	// Item Model
	makeFile(fmt.Sprintf("assets/iron_oak/models/item/%[1]s_%[2]s_sapling.json", ore, tree), fmt.Sprintf(`{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "iron_oak:block/%[1]s_%[2]s_sapling"
  }
}`, ore, tree))

	// Loot Table
	makeFile(fmt.Sprintf("data/iron_oak/loot_tables/blocks/%[1]s_%[2]s_sapling.json", ore, tree), fmt.Sprintf(`{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1.0,
      "bonus_rolls": 0.0,
      "entries": [
        {
          "type": "minecraft:item",
          "name": "iron_oak:%[1]s_%[2]s_sapling"
        }
      ],
      "conditions": [
        {
          "condition": "minecraft:survives_explosion"
        }
      ]
    }
  ]
}`, ore, tree))
}

// Replace file with content
func makeFile(relPath string, content string) {
	fullPath := path.Join("./src/main/resources", relPath)

	log.Printf("Writing file at %s\n", relPath)

	err := ioutil.WriteFile(fullPath, []byte(content), 0755)

	if err != nil {
		log.Fatalf("failed to write file %q: %s\n", relPath, err)
	}
}

// Replace file with JSON content
func makeJsonFile(relPath string, content any) {
	bContent, err := json.MarshalIndent(content, "", "  ")

	if err != nil {
		log.Fatalf("failed to marshall json for %q: %s\n", relPath, err)
	}

	makeFile(relPath, string(bContent))
}

// Reads existing language map
func readLanguageMap(lang string) map[string]string {
	langMap := map[string]string{}

	fullPath := path.Join("./src/main/resources", fmt.Sprintf("assets/iron_oak/lang/%s.json", lang))

	raw, err := ioutil.ReadFile(fullPath)
	if err != nil {
		log.Fatalf("failed to read lang file for %s: %s", lang, err)
	}

	err = json.Unmarshal(raw, &langMap)
	if err != nil {
		log.Fatalf("failed to unmarshal lang file for %s: %s", lang, err)
	}

	return langMap
}