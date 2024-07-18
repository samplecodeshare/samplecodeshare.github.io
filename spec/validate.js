const fs = require('fs');
const path = require('path');
const { Spectral, isError } = require('@stoplight/spectral-core');
const { bundleRuleset } = require('@stoplight/spectral-ruleset-migrator');
const { fetch } = require('@stoplight/spectral-runtime');
const { httpAndFileResolver } = require('@stoplight/spectral-utils');
const YAML = require('yaml');

// Function to load YAML file
const loadYamlFile = (filePath) => {
    const fileContent = fs.readFileSync(filePath, 'utf8');
    return YAML.parse(fileContent);
};

// Main function
const validateYaml = async (yamlFilePath, rulesFilePath) => {
    try {
        // Load and bundle ruleset
        const rulesetContent = fs.readFileSync(rulesFilePath, 'utf8');
        const bundledRuleset = await bundleRuleset(rulesetContent);

        // Initialize Spectral
        const spectral = new Spectral({ resolver: httpAndFileResolver });
        await spectral.setRuleset(bundledRuleset);

        // Load and parse the YAML content to be validated
        const yamlContent = loadYamlFile(yamlFilePath);

        // Validate the content
        const results = await spectral.run(yamlContent);

        // Output the results
        console.log(JSON.stringify(results, null, 2));
    } catch (error) {
        console.error('Error:', error.message);
    }
};

// Paths to the YAML files
const yamlFilePath = path.join(__dirname, 'openapi.yaml'); // Replace with your YAML file
const rulesFilePath = path.join(__dirname, 'rules.yaml'); // Replace with your rules YAML file

// Validate the YAML
validateYaml(yamlFilePath, rulesFilePath);
