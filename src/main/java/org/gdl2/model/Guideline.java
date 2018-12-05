package org.gdl2.model;

import lombok.Value;
import org.gdl2.resources.Language;
import org.gdl2.resources.ResourceDescription;
import org.gdl2.terminology.TermDefinition;
import org.gdl2.test.TestCase;

import java.util.List;

/**
 * Top level object representing a CDS guideline.
 */
@Value
public final class Guideline {
    private String id;
    private String gdlVersion;
    private String concept;
    private Language language;
    private ResourceDescription description;
    private GuideDefinition definition;
    private GuideOntology ontology;
    private List<TestCase> testCases;

    /**
     * Gets the term text in specified language.
     *
     * @param language the language of the term_definition to use
     * @param id       the id of the term
     * @return null if term_definition not found for given language or term not found for given id
     */
    public String getTermText(String language, String id) {
        TermDefinition termDefinition = getOntology().getTermDefinitions().get(language);
        return termDefinition == null ? null : termDefinition.getTermText(id);
    }
}
