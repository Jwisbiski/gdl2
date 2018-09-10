package org.gdl2.runtime;

import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;
import lombok.NonNull;
import org.gdl2.cdshooks.*;
import org.gdl2.datatypes.*;
import org.gdl2.expression.*;
import org.gdl2.model.*;
import org.gdl2.resources.Reference;
import org.gdl2.resources.ResourceDescription;
import org.gdl2.terminology.*;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Collections.*;
import static org.gdl2.cdshooks.Link.LinkType.ABSOLUTE;
import static org.gdl2.expression.OperatorKind.*;
import static org.gdl2.model.DataBinding.Type.INPUT;

/**
 * Java interpreter of GDL2 guidelines.
 */
public class Interpreter {
    static final String CURRENT_DATETIME = "currentDateTime";
    private static final String CURRENT_DATE = "currentDate";
    private static final Pattern VARIABLE_REGEX = Pattern.compile("\\{\\$gt([0-9.])+[a-zA-Z_0-9]*}");
    private static final String COUNT = "count";
    private static final String SUM = "sum";
    private static final String REFERENCE_NOT_FOUND = "Reference not found";
    private static final String ENGLISH_LANGUAGE = "en";
    private static final String TERM = "term";
    private static final String LOCAL = "local";
    private static final String GT = "gt";
    private static final String ABS = "abs";
    private static final String CEIL = "ceil";
    private static final String EXP = "exp";
    private static final String FLOOR = "floor";
    private static final String LOG = "log";
    private static final String LOG10 = "log10";
    private static final String LOG1P = "log1p";
    private static final String ROUND = "round";
    private static final String SQRT = "sqrt";
    private static final String ROOT = "/";
    private static final String CURRENT_INDEX = "current-index";

    private RuntimeConfiguration runtimeConfiguration;
    private static final TemplateFiller templateFiller = new TemplateFiller();
    private static final SubsumptionEvaluator defaultSubsumptionEvaluator = new DefaultSubsumptionEvaluator();

    public Interpreter() {
        this.runtimeConfiguration = defaultRuntimeConfiguration();
    }

    public Interpreter(RuntimeConfiguration runtimeConfiguration) {
        assertNotNull(runtimeConfiguration, "runtimeConfiguration can not be null");
        this.runtimeConfiguration = setDefaultRuntimeConfigurationIfMissing(runtimeConfiguration);
    }

    public Interpreter(ZonedDateTime currentDateTime) {
        assertNotNull(currentDateTime, "currentDateTime can not be null");
        this.runtimeConfiguration = RuntimeConfiguration.builder()
                .currentDateTime(currentDateTime)
                .language(ENGLISH_LANGUAGE)
                .objectCreatorPlugin(new DefaultObjectCreator())
                .terminologySubsumptionEvaluators(Collections.emptyMap())
                .build();
    }

    public Interpreter(ZonedDateTime currentDateTime, String language) {
        assertNotNull(currentDateTime, "currentDateTime can not be null");
        assertNotNull(language, "language can not be null");
        this.runtimeConfiguration = RuntimeConfiguration.builder()
                .currentDateTime(currentDateTime)
                .language(language)
                .objectCreatorPlugin(new DefaultObjectCreator())
                .terminologySubsumptionEvaluators(Collections.emptyMap())
                .build();
    }

    public Interpreter(String language) {
        assertNotNull(language, "language can not be null");
        this.runtimeConfiguration = RuntimeConfiguration.builder()
                .language(language)
                .objectCreatorPlugin(new DefaultObjectCreator())
                .terminologySubsumptionEvaluators(Collections.emptyMap())
                .build();
    }

    private RuntimeConfiguration defaultRuntimeConfiguration() {
        return RuntimeConfiguration.builder()
                .language(ENGLISH_LANGUAGE)
                .objectCreatorPlugin(new DefaultObjectCreator())
                .terminologySubsumptionEvaluators(Collections.emptyMap())
                .build();
    }

    private RuntimeConfiguration setDefaultRuntimeConfigurationIfMissing(RuntimeConfiguration runtimeConfiguration) {
        RuntimeConfiguration.RuntimeConfigurationBuilder runtimeConfigurationBuilder = RuntimeConfiguration.builder()
                .currentDateTime(runtimeConfiguration.getCurrentDateTime())
                .includingInputWithPredicate(runtimeConfiguration.isIncludingInputWithPredicate())
                .language(runtimeConfiguration.getLanguage() == null ? ENGLISH_LANGUAGE : runtimeConfiguration.getLanguage())
                .objectCreatorPlugin(runtimeConfiguration.getObjectCreatorPlugin() == null ? new DefaultObjectCreator() : runtimeConfiguration.getObjectCreatorPlugin())
                .terminologySubsumptionEvaluators(
                        runtimeConfiguration.getTerminologySubsumptionEvaluators() == null
                                ? Collections.emptyMap() : runtimeConfiguration.getTerminologySubsumptionEvaluators())
                .dateTimeFormatPattern(runtimeConfiguration.getDateTimeFormatPattern());
        if (runtimeConfiguration.getTimezoneId() != null) {
            runtimeConfigurationBuilder.timezoneId(runtimeConfiguration.getTimezoneId());
        }
        return runtimeConfigurationBuilder.build();
    }

    private static void assertNotNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public List<DataInstance> executeGuidelines(List<Guideline> guidelines, List<DataInstance> inputDataInstances) {
        return executeGuidelinesWithCards(guidelines, inputDataInstances, new ArrayList<>()).getResult();
    }

    public List<Card> executeCdsHooksGuidelines(List<Guideline> guidelines, List<DataInstance> inputDataInstances) {
        List<Card> cardList = new ArrayList<>();

        if (useCardsInRules(guidelines)) {
            executeGuidelinesWithCards(guidelines, inputDataInstances, cardList);
        } else {
            cardList = executeCdsHooksGuidelinesClassicMode(guidelines, inputDataInstances);
        }
        return cardList;
    }

    private List<Card> executeCdsHooksGuidelinesClassicMode(List<Guideline> guidelines, List<DataInstance> inputDataInstances) {
        List<Card> cardList = new ArrayList<>();
        List<DataInstance> dataInstances = executeGuidelines(guidelines, inputDataInstances);
        for (DataInstance dataInstance : dataInstances) {
            Card card = fetchCardFromDataInstance(dataInstance);
            if (card.getSummary() != null) {
                cardList.add(card);
            }
        }
        return cardList;
    }

    private Card fetchCardFromDataInstance(DataInstance dataInstance) {
        Gson gson = new Gson();
        String json = new Gson().toJson(dataInstance.getRoot());
        return gson.fromJson(json, Card.class);
    }

    private boolean useCardsInRules(List<Guideline> guidelines) {
        for (Guideline guideline : guidelines) {
            for (Rule rule : guideline.getDefinition().getRules().values()) {
                if (rule.getCards() != null && !rule.getCards().isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    // xxxxGetFiredRules methods are for gdl2-editor
    public ExecutionOutput executeGuidelinesAndGetFiredRules(List<Guideline> guidelines, List<DataInstance> inputDataInstances) {
        return executeGuidelinesWithCards(guidelines, inputDataInstances, new ArrayList<>());
    }

    public List<Card> executeCdsHooksGuidelinesAndGetFiredRules(List<Guideline> guidelines, List<DataInstance> inputDataInstances) {
        List<Card> cardList = new ArrayList<>();
        executeGuidelinesWithCards(guidelines, inputDataInstances, cardList);
        return cardList;
    }

    // TODO sort guidelines according to dependency
    private ExecutionOutput executeGuidelinesWithCards(List<Guideline> guidelines,
                                                       List<DataInstance> inputDataInstances,
                                                       List<Card> cards) {
        assertNotNull(guidelines, "List<Guideline> cannot be null.");
        assertNotNull(inputDataInstances, "List<DataInstance> cannot be null.");

        Map<String, DataInstance> allResults = new HashMap<>();
        List<DataInstance> input = new ArrayList<>(inputDataInstances);
        List<DataInstance> totalResult = new ArrayList<>();
        Map<String, Set<String>> firedRules = new LinkedHashMap<>();
        for (Guideline guide : guidelines) {
            ExecutionOutput resultPerExecution = executeSingleGuidelineWithCards(guide, input, cards);
            input = new ArrayList<>(inputDataInstances);
            for (DataInstance dataInstance : resultPerExecution.getResult()) {
                DataInstance existing = allResults.get(dataInstance.modelId());
                if (existing == null || isInputData(dataInstance, guide)) {
                    allResults.put(dataInstance.modelId(), dataInstance);
                } else if (isOutputTemplateData(dataInstance, guide)) {
                    input.add(dataInstance);
                } else {
                    existing.merge(dataInstance);
                }
            }
            input.addAll(allResults.values());
            totalResult.addAll(resultPerExecution.result);
            firedRules.putAll(resultPerExecution.getFiredRules());
        }
        return new ExecutionOutput(firedRules, totalResult);
    }

    private boolean isOutputTemplateData(DataInstance dataInstance, Guideline guideline) {
        if (guideline.getDefinition().getTemplates() == null) {
            return false;
        }
        return guideline.getDefinition().getTemplates().containsKey(dataInstance.id());
    }

    private boolean isInputData(DataInstance dataInstance, Guideline guideline) {
        if (guideline.getDefinition().getDataBindings() == null) {
            return false;
        }
        return guideline.getDefinition().getDataBindings().containsKey(dataInstance.id())
                && guideline.getDefinition().getDataBindings().get(dataInstance.id()).getType().equals(INPUT);
    }

    public List<DataInstance> executeSingleGuideline(Guideline guide, List<DataInstance> dataInstances) {
        return executeSingleGuidelineWithCards(guide, dataInstances, null).getResult();
    }

    private ExecutionOutput executeSingleGuidelineWithCards(Guideline guide, List<DataInstance> dataInstances,
                                                            List<Card> cards) {
        InternalOutput internalOutput = execute(guide, dataInstances, cards);
        List<DataInstance> resultDataInstances = collectDataInstancesFromValueListMap(internalOutput.getResult(), guide.getDefinition());
        return new ExecutionOutput(internalOutput.firedRules, resultDataInstances);
    }

    private Set<String> getCodesForAssignableVariables(GuideDefinition guideDefinition) {
        Set<String> codesFromAssignments = guideDefinition.getRules().entrySet().stream()
                .filter(s -> (s.getValue().getThen() != null))
                .flatMap(entry -> entry.getValue().getThen().stream())
                .filter(s -> !(s instanceof CreateInstanceExpression))
                .filter(s -> !(s instanceof UseTemplateExpression))
                .map(assignmentExpression -> ((AssignmentExpression) assignmentExpression).getVariable().getCode())
                .collect(Collectors.toSet());
        Set<String> codesFromCreateStatements = guideDefinition.getRules().entrySet().stream()
                .filter(s -> (s.getValue().getThen() != null))
                .flatMap(entry -> entry.getValue().getThen().stream())
                .filter(s -> s instanceof CreateInstanceExpression)
                .flatMap(createInstanceExpression -> ((CreateInstanceExpression) createInstanceExpression).getAssignmentExpressions().stream())
                .map(assignmentExpression -> assignmentExpression.getVariable().getCode())
                .collect(Collectors.toSet());
        if (guideDefinition.getDefaultActions() != null) {
            Set<String> codesFromDefaultActions = guideDefinition.getDefaultActions().stream()
                    .map(assignmentExpression -> ((AssignmentExpression) assignmentExpression).getVariable().getCode())
                    .collect(Collectors.toSet());
            codesFromAssignments.addAll(codesFromDefaultActions);
        }
        codesFromAssignments.addAll(codesFromCreateStatements);
        return codesFromAssignments;
    }

    /*Only used in testing*/
    InternalOutput execute(Guideline guideline, List<DataInstance> dataInstances) {
        return execute(guideline, dataInstances, null);
    }

    private InternalOutput execute(Guideline guideline, List<DataInstance> dataInstances, List<Card> cards) {
        assertNotNull(guideline, "Guideline cannot not be null.");
        assertNotNull(dataInstances, "List<DataInstance> cannot be null.");
        Map<String, List<Object>> selectedInput = selectDataInstancesUsingPredicatesAndSortWithElementBindingCode(
                dataInstances, guideline);
        Map<String, Set<String>> guidelineFiredRules = new LinkedHashMap<>();
        Map<String, Object> resultDefaultRuleExecution = new HashMap<>();
        Map<String, Class> typeMap = new HashMap<>();
        Set<String> firedRules = new LinkedHashSet<>();
        boolean allPreconditionsAreTrue = true;
        if (guideline.getDefinition().getPreConditions() != null) {
            allPreconditionsAreTrue = guideline.getDefinition().getPreConditions().stream()
                    .allMatch(expressionItem -> evaluateBooleanExpression(expressionItem, selectedInput, guideline, null));
        }
        if (!allPreconditionsAreTrue) {
            guidelineFiredRules.put(guideline.getId(), firedRules);
            return new InternalOutput(guidelineFiredRules, selectedInput);
        }
        if (guideline.getDefinition().getDefaultActions() != null) {
            for (ExpressionItem assignmentExpression : guideline.getDefinition().getDefaultActions()) {
                performAssignmentStatements((AssignmentExpression) assignmentExpression, selectedInput, typeMap,
                        resultDefaultRuleExecution, guideline);
                mergeValueMapIntoListValueMap(resultDefaultRuleExecution, selectedInput);
            }
        }
        List<Rule> sortedRules = sortRulesByPriority(guideline.getDefinition().getRules().values());

        Map<String, List<Object>> inputAndResult = new HashMap<>(selectedInput);
        for (Rule rule : sortedRules) {
            Map<String, List<Object>> resultPerRuleExecution = evaluateRule(rule, inputAndResult, guideline, firedRules, cards);
            mergeListValueMaps(resultPerRuleExecution, inputAndResult);
        }
        guidelineFiredRules.put(guideline.getId(), firedRules);
        return new InternalOutput(guidelineFiredRules, inputAndResult);
    }

    private void mergeValueMapIntoListValueMap(Map<String, Object> valueMap, Map<String, List<Object>> valueListMap) {
        for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
            valueListMap.computeIfAbsent(entry.getKey(), s -> new ArrayList<>()).add(entry.getValue());
        }
    }

    private void mergeListValueMaps(Map<String, List<Object>> valueMap, Map<String, List<Object>> valueListMap) {
        for (Map.Entry<String, List<Object>> entry : valueMap.entrySet()) {
            List<Object> values = valueListMap.computeIfAbsent(entry.getKey(), k -> new ArrayList<>());
            values.addAll(entry.getValue());
        }
    }

    private List<DataInstance> collectDataInstancesFromValueListMap(Map<String, List<Object>> valueListMap,
                                                                    GuideDefinition guideDefinition) {
        List<DataInstance> dataInstances = new ArrayList<>();
        Set<String> assignableCodes = getCodesForAssignableVariables(guideDefinition);
        if (guideDefinition.getTemplates() != null) {
            for (Map.Entry<String, Template> entry : guideDefinition.getTemplates().entrySet()) {
                Template template = entry.getValue();
                if (valueListMap.containsKey(template.getId())) {
                    List<Object> list = valueListMap.get(template.getId());
                    for (Object object : list) {
                        dataInstances.add(fromOutputTemplateObject(template, object));
                    }
                }
            }
        }
        if (guideDefinition.getDataBindings() == null) {
            return dataInstances;
        }
        for (DataBinding dataBinding : guideDefinition.getDataBindings().values()) {
            if (INPUT.equals(dataBinding.getType())) {
                if (!this.runtimeConfiguration.isIncludingInputWithPredicate()
                        || (this.runtimeConfiguration.isIncludingInputWithPredicate()
                        && (dataBinding.getPredicates() == null || dataBinding.getPredicates().size() == 0))) {
                    continue;
                }
            }
            Map<String, List<Object>> pathValueListMap = new HashMap<>();
            int total = 0;
            for (Map.Entry<String, Element> elementBindingEntry : dataBinding.getElements().entrySet()) {
                String elementId = elementBindingEntry.getValue().getId();
                String elementPath = elementBindingEntry.getValue().getPath();
                for (Map.Entry<String, List<Object>> entry : valueListMap.entrySet()) {
                    String valueKey = entry.getKey();
                    List<Object> objects = entry.getValue();
                    if (elementId.equals(valueKey)) {
                        if (this.runtimeConfiguration.isIncludingInputWithPredicate()
                                && INPUT.equals(dataBinding.getType())) {
                            if (total < objects.size()) {
                                total = objects.size();
                            }
                            pathValueListMap.put(elementPath, objects);
                        } else if (assignableCodes.contains(valueKey)) {
                            total = 1; // only take last element for output type
                            pathValueListMap.put(elementPath, singletonList(objects.get(objects.size() - 1)));
                        }
                    }
                }
            }
            dataInstances.addAll(
                    createFromValueListsUsingSingleDataBinding(dataBinding.getId(), dataBinding.getModelId(), pathValueListMap, total));
        }
        return dataInstances;
    }

    private DataInstance fromOutputTemplateObject(Template template, Object object) {
        DataInstance dataInstance = new DataInstance.Builder()
                .id(template.getId())
                .modelId(template.getModelId())
                .addValue(ROOT, object)
                .build();
        if (template.getElementBindings() != null) {
            Gson gson = new Gson();
            object = retrieveNamedObjectToAvoidNameInObjectPaths(object, template.getName());
            String json = gson.toJson(object);
            for (ElementBinding elementBinding : template.getElementBindings()) {
                String path = elementBinding.getPath();
                String jsonPath = "$" + path.replaceAll("/", ".");
                String type = elementBinding.getType();
                Object jsonPathValue = JsonPath.read(json, jsonPath);
                String value;
                Object objectValue;
                if ("DV_CODED_TEXT".equals(type)) {
                    value = gson.toJson(jsonPathValue);
                    objectValue = gson.fromJson(value, DvCodedText.class);
                } else if ("DV_TEXT".equals(type)) {
                    objectValue = DvText.valueOf(jsonPathValue.toString());
                } else {
                    objectValue = jsonPathValue;
                }
                if (objectValue != null) {
                    dataInstance.setValue(path, objectValue);
                }
            }
        }
        return dataInstance;
    }

    private Object retrieveNamedObjectToAvoidNameInObjectPaths(Object object, String name) {
        if (name == null) {
            return object;
        }
        return ((Map) object).get(name);
    }

    private List<DataInstance> createFromValueListsUsingSingleDataBinding(String bindingId,
                                                                          String modelId,
                                                                          Map<String, List<Object>> pathValueListMap,
                                                                          int total) {
        List<DataInstance> dataInstanceList = new ArrayList<>(total);
        for (int i = 0; i < total; i++) {
            DataInstance dataInstance = new DataInstance.Builder()
                    .id(bindingId)
                    .modelId(modelId)
                    .build();
            for (Map.Entry<String, List<Object>> entry : pathValueListMap.entrySet()) {
                String path = entry.getKey();
                List<Object> objects = entry.getValue();
                if (objects.size() > i) {
                    dataInstance.setValue(path, objects.get(i));
                }
            }
            if (dataInstance.values().size() != 0) {
                dataInstanceList.add(dataInstance);
            }
        }
        return dataInstanceList;
    }

    private Map<String, List<Object>> selectDataInstancesUsingPredicatesAndSortWithElementBindingCode(
            List<DataInstance> dataInstances, Guideline guideline) {
        if (guideline.getDefinition().getDataBindings() == null) {
            return emptyMap();
        }
        Map<String, List<Object>> valueListMap = new HashMap<>();
        for (Map.Entry<String, DataBinding> entry : guideline.getDefinition().getDataBindings().entrySet()) {
            DataBinding dataBinding = entry.getValue();
            List<DataInstance> selectedDataInstances =
                    evaluateDataInstancesWithPredicates(
                            filterDataInstancesWithModelId(dataInstances, dataBinding.getModelId()),
                            dataBinding.getPredicates(),
                            guideline);
            convertDataInstancesToCodeBasedValueMap(dataBinding, selectedDataInstances, valueListMap);
        }
        return valueListMap;
    }

    private void convertDataInstancesToCodeBasedValueMap(DataBinding dataBinding,
                                                         List<DataInstance> dataInstances,
                                                         Map<String, List<Object>> valueListMap) {
        Map<String, String> pathToCode = pathToCode(dataBinding);
        dataInstances.stream()
                .flatMap(s -> s.values().entrySet().stream())
                .filter(s -> pathToCode.containsKey(s.getKey()))
                .forEach(s -> valueListMap
                        .computeIfAbsent(pathToCode.get(s.getKey()), key -> new ArrayList<>())
                        .add(s.getValue()));
        for (DataInstance dataInstance : dataInstances) {
            if (dataInstance.getRoot() != null) {
                List<Object> valueList = valueListMap.computeIfAbsent(dataBinding.getId(), k -> new ArrayList<>());
                valueList.add(dataInstance.getRoot());
            }
        }
    }

    private Map<String, String> pathToCode(DataBinding dataBinding) {
        if (dataBinding.getElements() == null) {
            return EMPTY_MAP;
        }
        return dataBinding.getElements().entrySet().stream()
                .map(Map.Entry::getValue)
                .collect(Collectors.toMap(Element::getPath, Element::getId));
    }

    private List<Rule> sortRulesByPriority(Collection<Rule> rules) {
        return rules.stream().sorted(new RuleComparator()).collect(Collectors.toList());
    }

    private Map<String, List<Object>> evaluateRule(Rule rule, Map<String, List<Object>> input, Guideline guideline,
                                                   Set<String> firedRules, List<Card> cards) {
        Map<String, List<Object>> result = new HashMap<>();
        Map<String, Object> singleResult = new HashMap<>();
        boolean allWhenStatementsAreTrue = rule.getWhen() == null || rule.getWhen().stream()
                .allMatch(whenStatement -> evaluateBooleanExpression(whenStatement, input, guideline, firedRules));
        if (!allWhenStatementsAreTrue) {
            return result;
        }
        if (rule.getThen() != null) {
            Map<String, Class> typeMap = typeBindingThroughAssignmentStatements(rule.getThen());
            Map<String, Template> templateMap = guideline.getDefinition().getTemplates();
            for (ExpressionItem thenStatement : rule.getThen()) {
                if (thenStatement instanceof AssignmentExpression) {
                    performAssignmentStatements((AssignmentExpression) thenStatement, input, typeMap, singleResult, guideline);
                }
                if (thenStatement instanceof UseTemplateExpression) {
                    mergeValueMapIntoListValueMap(singleResult, result);
                    performUseTemplateStatement((UseTemplateExpression) thenStatement, templateMap, input, result, guideline);
                }
                if (hasContinuousAssignments(rule) || hasCards(rule)) {
                    mergeValueMapIntoListValueMap(singleResult, input);
                }
            }
        }
        if (hasCards(rule)) {
            for (Card card : rule.getCards()) {
                cards.add(processCard(card, input, guideline));
            }
        }
        firedRules.add(rule.getId());
        mergeValueMapIntoListValueMap(singleResult, result);
        input.remove(CURRENT_INDEX);
        return result;
    }

    private boolean hasCards(Rule rule) {
        return rule.getCards() != null && !rule.getCards().isEmpty();
    }

    /*
     * continuous assignments are more than one thenStatements assigning values to the same variable of a given rule
     */
    private boolean hasContinuousAssignments(Rule rule) {
        List<ExpressionItem> assignmentExpressions = rule.getThen();
        if (assignmentExpressions == null || assignmentExpressions.size() <= 1) {
            return false;
        }
        Set<String> variableIds = new HashSet<>();
        for (ExpressionItem expressionItem : assignmentExpressions) {
            if (!(expressionItem instanceof AssignmentExpression)) {
                continue;
            }
            AssignmentExpression assignmentExpression = (AssignmentExpression) expressionItem;
            String code = assignmentExpression.getVariable().getCode();
            if (variableIds.contains(code)) {
                return true;
            } else {
                variableIds.add(code);
            }
        }
        return false;
    }

    private Card processCard(Card card, Map<String, List<Object>> input, Guideline guideline) {
        TermDefinition termDefinition = guideline.getOntology().getTermDefinitions().get(this.runtimeConfiguration.getLanguage());
        if (termDefinition == null) {
            termDefinition = guideline.getOntology().getTermDefinitions().get(ENGLISH_LANGUAGE);
        }
        List<Suggestion> suggestions = new ArrayList<>();
        if (card.getSuggestions() != null) {
            for (int i = 0, j = card.getSuggestions().size(); i < j; i++) {
                suggestions.add(processSuggestion(card.getSuggestions().get(i), input, guideline, termDefinition));
            }
        }
        Source source = card.getSource();
        if (source != null) {
            source = processReferencedSource(source, guideline.getDescription());
        }
        List<Link> links = new ArrayList<>();
        if (card.getLinks() != null) {
            for (Link link : card.getLinks()) {
                links.add(processLink(link, guideline.getDescription(), input, termDefinition));
            }
        }
        return Card.builder()
                .summary(replaceVariablesWithValues(card.getSummary(), input, termDefinition))
                .detail(replaceVariablesWithValues(card.getDetail(), input, termDefinition))
                .indicator(card.getIndicator())
                .source(source)
                .suggestions(suggestions)
                .links(links)
                .build();
    }

    private Source processReferencedSource(Source source, ResourceDescription description) {
        if (description == null) {
            return source;
        }
        String label = fromReferencedLabel(source.getLabelReference(), description);
        URL url = fromReferencedUrl(source.getUrlReference(), description);
        return Source.builder().label(label).url(url).build();
    }

    private Link processLink(Link link, ResourceDescription resourceDescription,
                             Map<String, List<Object>> values, TermDefinition termDefinition) {
        if (ABSOLUTE.equals(link.getType()) && resourceDescription != null) {
            return processReferencedLink(link, resourceDescription);
        } else {
            return processLanguageSpecificLabel(link, values, termDefinition);
        }
    }

    private Link processReferencedLink(Link link, ResourceDescription resourceDescription) {
        String label = fromReferencedLabel(link.getLabelReference(), resourceDescription);
        URL url = fromReferencedUrl(link.getUrlReference(), resourceDescription);
        return Link.builder()
                .label(label)
                .url(url)
                .type(ABSOLUTE)
                .build();
    }

    private Link processLanguageSpecificLabel(Link link, Map<String, List<Object>> values, TermDefinition termDefinition) {
        return Link.builder()
                .label(replaceVariablesWithValues(link.getLabel(), values, termDefinition))
                .url(link.getUrl())
                .type(link.getType())
                .build();
    }

    private String fromReferencedLabel(String labelRef, ResourceDescription resourceDescription) {
        String label = "";
        if (labelRef != null && labelRef.startsWith("$ref[") && labelRef.endsWith("].label")) {
            int index = Integer.parseInt(labelRef.substring(5, labelRef.indexOf("]"))) - 1;
            if (index < resourceDescription.getReferences().size()) {
                label = resourceDescription.getReferences().get(index).getLabel();
            }
        }
        return label;
    }

    private URL fromReferencedUrl(String urlReference, ResourceDescription resourceDescription) {
        URL url = null;
        if (urlReference != null && urlReference.startsWith("$ref[") && urlReference.endsWith("].url")) {
            int index = Integer.parseInt(urlReference.substring(5, urlReference.indexOf("]"))) - 1;
            if (index < resourceDescription.getReferences().size()) {
                try {
                    url = new URL(resourceDescription.getReferences().get(index).getUrl());
                } catch (MalformedURLException murle) {
                    // ignore
                }
            }
        }
        return url;
    }

    private String replaceVariablesWithValues(String source, Map<String, List<Object>> values, TermDefinition termDefinition) {
        if (source == null) {
            return null;
        }
        StringBuffer stringBuffer = new StringBuffer();
        Matcher matcher = VARIABLE_REGEX.matcher(source);
        while (matcher.find()) {
            String text = matcher.group();
            String expression = text.substring(1, text.length() - 1);
            Variable variable = parseVariable(expression);
            Object value;
            if (TERM.equals(variable.getAttribute())) {
                value = getStringValueFromTerm(variable, termDefinition);
            } else {
                value = evaluateExpressionItem(variable, values);
            }
            if (value != null) {
                matcher.appendReplacement(stringBuffer, value.toString());
            }
        }
        matcher.appendTail(stringBuffer);
        return stringBuffer.toString();
    }

    private String getStringValueFromTerm(Variable variable, TermDefinition termDefinition) {
        Term term = termDefinition.getTerms().get(variable.getCode());
        return term == null ? null : term.getText();
    }

    private Variable parseVariable(String expression) {
        int index = expression.indexOf(".");
        if (index < 0) {
            return new Variable(expression.substring(1));
        } else {
            return new Variable(expression.substring(1, index), null, null, expression.substring(index + 1));
        }
    }

    private Suggestion processSuggestion(Suggestion suggestion, Map<String, List<Object>> input, Guideline guideline,
                                         TermDefinition termDefinition) {
        List<Action> actions = new ArrayList<>();
        if (suggestion.getActions() != null) {
            for (Action action : suggestion.getActions()) {
                actions.add(processAction(action, input, guideline, termDefinition));
            }
        }
        return Suggestion.builder().actions(actions)
                .label(replaceVariablesWithValues(suggestion.getLabel(), input, termDefinition))
                .uuid(suggestion.getUuid())
                .build();
    }

    private Action processAction(Action action, Map<String, List<Object>> input, Guideline guideline, TermDefinition termDefinition) {
        Action.ActionBuilder actionBuilder = Action.builder()
                .description(replaceVariablesWithValues(action.getDescription(), input, termDefinition))
                .type(action.getType());
        if (action.getResourceTemplate() != null) {
            actionBuilder.resource(processUseTemplate(action.getResourceTemplate(), input, guideline));
        }
        return actionBuilder.build();
    }

    private Object processUseTemplate(UseTemplate useTemplate, Map<String, List<Object>> input, Guideline guideline) {
        Template template = null;
        if (guideline.getDefinition().getTemplates() != null) {
            template = guideline.getDefinition().getTemplates().get(useTemplate.getTemplateId());
        }
        if (template == null) {
            return null;
        }
        Map<String, Object> useTemplateLocalResult = new HashMap<>();
        for (ExpressionItem expressionItem : useTemplate.getAssignments()) {
            AssignmentExpression assignmentExpression = (AssignmentExpression) expressionItem;
            Object value = evaluateExpressionItem(assignmentExpression.getAssignment(), input, guideline, null);
            useTemplateLocalResult.put(assignmentExpression.getVariable().getCode(), value);
        }

        Map<String, Object> localMapCopy = deepCopy(template.getObject());
        this.templateFiller.traverseMapAndReplaceAllVariablesWithValues(localMapCopy, useTemplateLocalResult, input);
        try {
            return this.runtimeConfiguration.getObjectCreatorPlugin().create(template.getModelId(), localMapCopy);
        } catch (ClassNotFoundException cnf) {
            System.out.println("failed to create object using template(" + template.getModelId() + "), class not found..");
            cnf.printStackTrace();
            return null;
        }
    }

    private Map<String, Object> deepCopy(Map<String, Object> map) {
        Gson gson = new Gson();
        String json = gson.toJson(map);
        return gson.fromJson(json, Map.class);
    }

    // mainly to resolve ambiguity between DvCount and DvQuantity
    Map<String, Class> typeBindingThroughAssignmentStatements(List<ExpressionItem> assignmentExpressions) {
        Map<String, Set<String>> attributesMap = new HashMap<>();
        for (ExpressionItem expressionItem : assignmentExpressions) {
            if (expressionItem instanceof AssignmentExpression) {
                Variable variable = ((AssignmentExpression) expressionItem).getVariable();
                attributesMap
                        .computeIfAbsent(variable.getCode(), key -> new HashSet<>())
                        .add(variable.getAttribute());
            }
        }
        TypeBinding typeBinding = new TypeBinding();
        return attributesMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, s -> typeBinding.possibleType(s.getValue())));
    }

    void performAssignmentStatements(AssignmentExpression assignmentExpression, Map<String, List<Object>> input,
                                     Map<String, Class> typeMap, Map<String, Object> result) {
        performAssignmentStatements(assignmentExpression, input, typeMap, result, null);
    }

    void performAssignmentStatements(AssignmentExpression assignmentExpression, Map<String, List<Object>> input,
                                     Map<String, Class> typeMap, Map<String, Object> result, Guideline guideline) {
        Variable variable = assignmentExpression.getVariable();
        String attribute = variable.getAttribute();
        if (assignmentExpression instanceof CreateInstanceExpression) {
            evaluateCreateInstanceExpression(assignmentExpression, input, typeMap, result, guideline);
            return;
        }
        Object value;
        if (assignmentExpression.getAssignment() instanceof QuantityConstant) {
            value = ((QuantityConstant) assignmentExpression.getAssignment()).getQuantity();
        } else {
            value = evaluateExpressionItem(assignmentExpression.getAssignment(), input, guideline, null);
        }
        if (TypeBinding.PRECISION.equals(attribute)) {
            DvQuantity dvQuantity = retrieveDvQuantityFromResultMapOrCreateNew(variable.getCode(), result);
            try {
                if (value instanceof String) {
                    DvQuantity newQuantity = new DvQuantity(dvQuantity.getUnit(), dvQuantity.getMagnitude(), Integer.parseInt((String) value));
                    result.put(variable.getCode(), newQuantity);
                } else if (value instanceof Integer) {
                    DvQuantity newQuantity = new DvQuantity(dvQuantity.getUnit(), dvQuantity.getMagnitude(), (Integer) value);
                    result.put(variable.getCode(), newQuantity);
                } else {
                    throw new IllegalArgumentException("Unexpected integer value: " + value + ", in assignmentExpression: " + assignmentExpression);
                }
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("Unexpected integer string value: " + value + ", in assignmentExpression: " + assignmentExpression);
            }
        } else if (TypeBinding.UNIT.equals(attribute)) {
            DvQuantity dvQuantity = retrieveDvQuantityFromResultMapOrCreateNew(variable.getCode(), result);
            if (value instanceof String) {
                DvQuantity newQuantity = new DvQuantity((String) value, dvQuantity.getMagnitude(), dvQuantity.getPrecision());
                result.put(variable.getCode(), newQuantity);
            } else {
                throw new IllegalArgumentException("Unexpected value for units: " + value + ", in assignmentExpression: " + assignmentExpression);
            }
        } else if (TypeBinding.MAGNITUDE.equals(attribute)) {
            performAssignMagnitudeAttribute(value, variable, assignmentExpression, input, typeMap, result);
        } else if (TypeBinding.NULL_FLAVOR.equals(attribute)) {
            if (value instanceof DvCodedText) {
                result.put(variable.getCode() + "." + TypeBinding.NULL_FLAVOR, value);
            } else {
                throw new IllegalArgumentException("Unexpected value: " + value + ", in null_flavor assignmentExpression: " + assignmentExpression);
            }
        } else if (TypeBinding.VALUE.equals(attribute) && value instanceof String) {
            result.put(variable.getCode(), DvText.valueOf((String) value));
        } else if ("true".equalsIgnoreCase(value.toString()) || "false".equalsIgnoreCase(value.toString())) {
            result.put(variable.getCode(), Boolean.valueOf(value.toString()));
        } else if (value instanceof DvCodedText && guideline != null) {
            DvCodedText dvCodedText = findTermOfDesignatedLanguage((DvCodedText) value,
                    guideline.getOntology().getTermDefinitions());
            result.put(assignmentExpression.getVariable().getCode(), dvCodedText);
        } else {
            result.put(assignmentExpression.getVariable().getCode(), value);
        }
    }

    private boolean isGuidelineTerm(CodePhrase codePhrase) {
        return LOCAL.equals(codePhrase.getTerminology()) && codePhrase.getCode().startsWith(GT);
    }

    private DvCodedText findTermOfDesignatedLanguage(DvCodedText dvCodedText, Map<String, TermDefinition> termDefinitionMap) {
        if (!isGuidelineTerm(dvCodedText.getDefiningCode())) {
            return dvCodedText;
        }
        TermDefinition termDefinition = termDefinitionMap.get(this.runtimeConfiguration.getLanguage());
        if (termDefinition == null) {
            return dvCodedText;
        }
        Term term = termDefinition.getTerms().get(dvCodedText.getDefiningCode().getCode());
        if (term == null) {
            return dvCodedText;
        }
        return DvCodedText.builder()
                .definingCode(dvCodedText.getDefiningCode())
                .value(term.getText())
                .build();
    }

    private void performUseTemplateStatement(UseTemplateExpression useTemplateExpression, Map<String, Template> templateMap,
                                             Map<String, List<Object>> input, Map<String, List<Object>> result, Guideline guideline) {
        Variable variable = useTemplateExpression.getVariable();
        String attribute = variable.getCode();
        Template template = templateMap.get(attribute);
        if (template == null) {
            return;
        }
        Map<String, Object> useTemplateLocalResult = new HashMap<>();
        for (AssignmentExpression assignmentExpression : useTemplateExpression.getAssignmentExpressions()) {
            Object value = evaluateExpressionItem(assignmentExpression.getAssignment(), input, guideline, null);
            useTemplateLocalResult.put(assignmentExpression.getVariable().getCode(), value);
        }
        useTemplateLocalResult.putAll(result);
        Map<Variable, List<Variable>> inputVariableMap = useTemplateExpression.getInputVariableMap();
        if (inputVariableMap == null || inputVariableMap.size() == 0) {
            createObjectUsingOutPutTemplate(variable, template, useTemplateLocalResult, input, result, null);
        } else {
            List<Variable> ifVariables = useTemplateExpression.getIfVariables();
            for (int i = 0, j = inputVariableMap.entrySet().iterator().next().getValue().size(); i < j; i++) {
                if (ifVariables != null && ifVariables.size() > i) {
                    Variable ifVariable = ifVariables.get(i);
                    Object value = retrieveValueFromValueMap(ifVariable, input);
                    if (Boolean.FALSE.equals(value)) {
                        continue;
                    }
                }
                Map<String, Object> values = createInputValueMap(inputVariableMap, input, i);
                if (!values.isEmpty()) {
                    createObjectUsingOutPutTemplate(variable, template, useTemplateLocalResult, input, result, values);
                }
            }
        }
    }

    private Map<String, Object> createInputValueMap(Map<Variable, List<Variable>> inputVariableMap, Map<String, List<Object>> input, int index) {
        Map<String, Object> valueMap = new HashMap<>();
        for (Map.Entry<Variable, List<Variable>> entry : inputVariableMap.entrySet()) {
            Variable variable = entry.getKey();
            List<Variable> variableList = entry.getValue();
            if (index < variableList.size()) {
                Variable inputVariable = variableList.get(index);
                Object value = retrieveValueFromValueMap(inputVariable, input);
                if (value != null) {
                    valueMap.put(variable.getCode(), value);
                }
            }
        }
        return valueMap;
    }

    private void createObjectUsingOutPutTemplate(Variable variable, Template template, Map<String, Object> useTemplateLocalResult,
                                                 Map<String, List<Object>> input, Map<String, List<Object>> result,
                                                 Map<String, Object> additionalInputValues) {
        Map<String, Object> localMapCopy = deepCopy(template.getObject());
        addCurrentDateTimeToGlobalVariableValues(input);
        this.templateFiller.traverseMapAndReplaceAllVariablesWithValues(localMapCopy, useTemplateLocalResult, input, additionalInputValues);

        String modelId = template.getModelId();
        try {
            Object object = this.runtimeConfiguration.getObjectCreatorPlugin().create(modelId, localMapCopy);
            List<Object> valueList = result.computeIfAbsent(variable.getCode(), k -> new ArrayList<>());
            if (template.getName() != null) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put(template.getName(), object);
                object = map;
            }
            valueList.add(object);
        } catch (ClassNotFoundException cnf) {
            System.out.println("failed to create object using template(" + template.getModelId() + "), class not found..");
            cnf.printStackTrace();
        }
    }

    private void addCurrentDateTimeToGlobalVariableValues(Map<String, List<Object>> valueMap) {
        valueMap.put(CURRENT_DATETIME, singletonList(systemCurrentDateTime()));
        valueMap.put(CURRENT_DATE, singletonList(systemCurrentDateTime()));
    }

    private void performAssignMagnitudeAttribute(Object value, Variable variable, AssignmentExpression assignmentExpression,
                                                 Map<String, List<Object>> input, Map<String, Class> typeMap, Map<String, Object> result) {
        Class type = typeMap.get(variable.getCode());
        DvQuantity dvQuantity = null;
        List<Object> valueList = input.get(variable.getCode());
        if (valueList != null && !valueList.isEmpty() && (valueList.get(valueList.size() - 1) instanceof DvQuantity)) {
            dvQuantity = (DvQuantity) valueList.get(valueList.size() - 1);
        }
        if (dvQuantity != null || DvQuantity.class.equals(type)) {
            if (dvQuantity == null) {
                dvQuantity = retrieveDvQuantityFromResultMapOrCreateNew(variable.getCode(), result);
            }
            try {
                Double magnitude;
                if (value instanceof Double) {
                    magnitude = (Double) value;
                } else if (value instanceof Long) {
                    magnitude = ((Long) value).doubleValue();
                    result.put(variable.getCode(), new DvQuantity(dvQuantity.getUnit(), ((Long) value).doubleValue(), dvQuantity.getPrecision()));
                } else if (value instanceof Integer) {
                    magnitude = ((Integer) value).doubleValue();
                    result.put(variable.getCode(), new DvQuantity(dvQuantity.getUnit(), ((Integer) value).doubleValue(), dvQuantity.getPrecision()));
                } else if (value instanceof String) {
                    magnitude = Double.parseDouble((String) value);
                } else {
                    throw new IllegalArgumentException("Unexpected double value: " + value + ", in assignmentExpression: " + assignmentExpression);
                }
                result.put(variable.getCode(), new DvQuantity(dvQuantity.getUnit(), magnitude, dvQuantity.getPrecision()));
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("Unexpected integer string value: " + value + ", in assignmentExpression: " + assignmentExpression);
            }
        } else {
            try {
                if (value instanceof Double) {
                    int intValue = ((Double) value).intValue();
                    result.put(variable.getCode(), new DvCount(intValue));
                } else if (value instanceof Integer) {
                    int intValue = (Integer) value;
                    result.put(variable.getCode(), new DvCount(intValue));
                } else {
                    throw new IllegalArgumentException("Unexpected double value: " + value + ", in assignmentExpression: " + assignmentExpression);
                }
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("Unexpected integer string value: " + value + ", in assignmentExpression: " + assignmentExpression);
            }
        }
    }

    private void evaluateCreateInstanceExpression(AssignmentExpression assignmentExpression, Map<String, List<Object>> input,
                                                  Map<String, Class> typeMap, Map<String, Object> result, Guideline guideline) {
        CreateInstanceExpression createInstanceExpression = (CreateInstanceExpression) assignmentExpression;
        List<AssignmentExpression> assignmentExpressions = createInstanceExpression.getAssignmentExpressions();
        for (AssignmentExpression expression : assignmentExpressions) {
            performAssignmentStatements(expression, input, typeMap, result, guideline);
            mergeValueMapIntoListValueMap(result, input);
        }
        result.putAll(result);
    }

    private DvQuantity retrieveDvQuantityFromResultMapOrCreateNew(String code, Map<String, Object> resultMap) {
        Object dataValue = resultMap.computeIfAbsent(code, k -> new DvQuantity(0));
        if (!(dataValue instanceof DvQuantity)) {
            throw new IllegalArgumentException("Expected DvQuantity but instead got: " + resultMap.get(code).getClass());
        }
        return (DvQuantity) dataValue;
    }

    Object evaluateExpressionItem(ExpressionItem expressionItem, Map<String, List<Object>> input) {
        return evaluateExpressionItem(expressionItem, input, null, null);
    }

    Object evaluateExpressionItem(ExpressionItem expressionItem, Map<String, List<Object>> input,
                                  Guideline guideline, Set<String> firedRules) {
        if (expressionItem instanceof ConstantExpression) {
            return evaluateConstantExpression(expressionItem);
        } else if (expressionItem instanceof ReferenceVariable) {
            ReferenceVariable referenceVariable = (ReferenceVariable) expressionItem;
            return retrieveReferenceVariableValueFromValueMap(referenceVariable, guideline.getDescription());
        } else if (expressionItem instanceof Variable) {
            Variable variable = (Variable) expressionItem;
            return retrieveValueFromValueMap(variable, input);
        } else if (expressionItem instanceof BinaryExpression) {
            return processBinaryExpression(expressionItem, input, guideline, firedRules);
        } else if (expressionItem instanceof UnaryExpression) {
            return processUnaryExpression((UnaryExpression) expressionItem, input, guideline, firedRules);
        } else if (expressionItem instanceof AnyExpression) {
            return processAnyExpression((AnyExpression) expressionItem, input, guideline, firedRules);
        } else if (expressionItem instanceof FunctionalExpression) {
            return processFunctionalExpression((FunctionalExpression) expressionItem, input, guideline, firedRules);
        } else {
            throw new IllegalArgumentException("Unsupported expressionItem: " + expressionItem);
        }
    }

    private String retrieveReferenceVariableValueFromValueMap(ReferenceVariable referenceVariable, ResourceDescription resourceDescription) {
        if (resourceDescription == null) {
            return REFERENCE_NOT_FOUND;
        }
        List<Reference> references = resourceDescription.getReferences();
        if (references == null || references.isEmpty()
                || references.size() < referenceVariable.getIndex()
                || referenceVariable.getIndex() <= 0) {
            return REFERENCE_NOT_FOUND;
        }
        Reference reference = references.get(referenceVariable.getIndex() - 1);
        if ("label".equalsIgnoreCase(referenceVariable.getAttribute())) {
            return reference.getLabel();
        } else if ("url".equalsIgnoreCase(referenceVariable.getAttribute())) {
            return reference.getUrl();
        }
        return REFERENCE_NOT_FOUND;
    }

    private Object evaluateConstantExpression(ExpressionItem expressionItem) {
        if (expressionItem instanceof DoubleConstant) {
            return ((DoubleConstant) expressionItem).getDoubleValue();
        } else if (expressionItem instanceof IntegerConstant) {
            return ((IntegerConstant) expressionItem).getIntegerValue();
        } else if (expressionItem instanceof CodedTextConstant) {
            return ((CodedTextConstant) expressionItem).getCodedText();
        } else if (expressionItem instanceof OrdinalConstant) {
            return ((OrdinalConstant) expressionItem).getOrdinal();
        } else if (expressionItem instanceof QuantityConstant) {
            return evaluateQuantityValue(((QuantityConstant) expressionItem).getQuantity());
        } else if (expressionItem instanceof DateTimeConstant) {
            return DvDateTime.valueOf(((DateTimeConstant) expressionItem).getValue());
        }
        ConstantExpression constantExpression = (ConstantExpression) expressionItem;
        String value = constantExpression.getValue();
        if ("null".equalsIgnoreCase(value)) {
            return null;
        } else if (value.startsWith("(-") && value.endsWith(")")) {
            int length = value.length();
            return value.substring(1, length - 1);
        } else {
            return value;
        }
    }

    private Object evaluateQuantityValue(@NonNull DvQuantity dvQuantity) {
        if (isTimePeriodUnits(dvQuantity.getUnit())) {
            return convertTimeQuantityToPeriodOrMilliSeconds(dvQuantity);
        }
        return dvQuantity.getMagnitude();
    }

    private boolean isTimePeriodUnits(String unit) {
        return "a".equals(unit) || "mo".equals(unit) || "d".equals(unit) || "h".equals(unit);
    }

    private Object convertTimeQuantityToPeriodOrMilliSeconds(DvQuantity dvQuantity) {
        int magnitude = Double.valueOf(dvQuantity.getMagnitude()).intValue();
        if ("a".equals(dvQuantity.getUnit())) {
            return Period.ofYears(magnitude);
        } else if ("mo".equals(dvQuantity.getUnit())) {
            return Period.ofMonths(magnitude);
        } else if ("d".equals(dvQuantity.getUnit())) {
            return Period.ofDays(magnitude);
        } else if ("h".equals(dvQuantity.getUnit())) {
            return Duration.ofHours((long) dvQuantity.getMagnitude());
        }
        throw new UnsupportedOperationException("Unsupported time period unit: " + dvQuantity.getUnit());
    }

    private boolean evaluateBooleanExpression(ExpressionItem whenStatement, Map<String, List<Object>> input,
                                              Guideline guideline, Set<String> firedRules) {
        Object value = evaluateExpressionItem(whenStatement, input, guideline, firedRules);
        return value instanceof Boolean && ((Boolean) value);
    }

    private Object processFunctionalExpression(FunctionalExpression functionalExpression, Map<String,
            List<Object>> input, Guideline guideline, Set<String> firedRules) {
        String function = functionalExpression.getFunction().toString();
        Double value = Double.valueOf(evaluateExpressionItem(functionalExpression.getItems().get(0), input, guideline, firedRules).toString());
        if (ABS.equalsIgnoreCase(function)) {
            return Math.abs(value);
        } else if (CEIL.equalsIgnoreCase(function)) {
            return Math.ceil(value);
        } else if (EXP.equalsIgnoreCase(function)) {
            return Math.exp(value);
        } else if (FLOOR.equalsIgnoreCase(function)) {
            return Math.floor(value);
        } else if (LOG.equalsIgnoreCase(function)) {
            return Math.log(value);
        } else if (LOG10.equalsIgnoreCase(function)) {
            return Math.log10(value);
        } else if (LOG1P.equalsIgnoreCase(function)) {
            return Math.log1p(value);
        } else if (ROUND.equalsIgnoreCase(function)) {
            return Math.round(value);
        } else if (SQRT.equalsIgnoreCase(function)) {
            return Math.sqrt(value);
        } else {
            throw new UnsupportedOperationException("Unsupported function: " + function);
        }
    }

    private Object processUnaryExpression(UnaryExpression unaryExpression, Map<String, List<Object>> input,
                                          Guideline guideline, Set<String> firedRules) {
        if (OperatorKind.FIRED.equals(unaryExpression.getOperator())) {
            return firedRules.contains(((Variable) unaryExpression.getOperand()).getCode());
        } else if (OperatorKind.NOT_FIRED.equals(unaryExpression.getOperator())) {
            return !firedRules.contains(((Variable) unaryExpression.getOperand()).getCode());
        } else if (OperatorKind.NOT.equals(unaryExpression.getOperator())) {
            return !Boolean.valueOf(evaluateExpressionItem(unaryExpression.getOperand(), input, guideline, firedRules).toString());
        } else {
            throw new UnsupportedOperationException("Unsupported unary operation: " + unaryExpression);
        }
    }

    private Object processAnyExpression(AnyExpression anyExpression, Map<String, List<Object>> input,
                                        Guideline guideline, Set<String> firedRules) {
        List<String> idList = new ArrayList<>();
        for (Variable variable : anyExpression.getInputVariables()) {
            idList.add(variable.getCode());
        }
        int maxListSize = maxValueListSize(input, idList);
        List currentIndex;
        Map<String, Integer> indexMap;
        if (input.containsKey(CURRENT_INDEX)) {
            currentIndex = input.get(CURRENT_INDEX);
            indexMap = (Map) currentIndex.get(0);
        } else {
            indexMap = new HashMap<>();
            currentIndex = singletonList(indexMap);
            input.put(CURRENT_INDEX, currentIndex);
        }
        for (int i = 0; i < maxListSize; i++) {
            if (Boolean.valueOf(
                    evaluateExpressionItem(
                            anyExpression.getOperand(),
                            createSingletonListByIndex(idList, input, i),
                            guideline,
                            firedRules).toString())) {

                for (Variable variable : anyExpression.getInputVariables()) {
                    indexMap.put(variable.getCode(), i);
                }
                return true;
            }
        }
        return false;
    }

    private int maxValueListSize(Map<String, List<Object>> input, List<String> idList) {
        int max = 0;
        for (String id : idList) {
            List<Object> list = input.get(id);
            if (list == null) {
                continue;
            }
            if (max < list.size()) {
                max = list.size();
            }
        }
        return max;
    }

    private Map<String, List<Object>> createSingletonListByIndex(List<String> idList, Map<String, List<Object>> input, int index) {
        Map<String, List<Object>> singletonListValueMap = new HashMap<>(input);
        for (String id : idList) {
            List<Object> valueList = input.get(id);
            if (valueList == null) {
                continue;
            }
            if (valueList.size() <= index) {
                singletonListValueMap.put(id, singletonList(valueList.get(valueList.size() - 1)));
            } else {
                singletonListValueMap.put(id, singletonList(valueList.get(index)));
            }
        }
        return singletonListValueMap;
    }

    private Object processBinaryExpression(ExpressionItem expressionItem, Map<String, List<Object>> input,
                                           Guideline guideline, Set<String> firedRules) {
        BinaryExpression binaryExpression = (BinaryExpression) expressionItem;
        OperatorKind operator = binaryExpression.getOperator();
        ExpressionItem leftExpression = binaryExpression.getLeft();
        ExpressionItem rightExpression = binaryExpression.getRight();
        if (operator == OR) {
            if (leftExpression == null) {
                throw new IllegalArgumentException("Null value in left expression item with OR operator: " + expressionItem);
            }
            return evaluateBooleanExpression(leftExpression, input, guideline, firedRules)
                    || evaluateBooleanExpression(rightExpression, input, guideline, firedRules);
        }
        Object leftValue = leftExpression == null ? null : evaluateExpressionItem(leftExpression, input, guideline, firedRules);
        Object rightValue = rightExpression == null ? null : evaluateExpressionItem(rightExpression, input, guideline, firedRules);
        if (leftValue instanceof TemporalAmount || rightValue instanceof TemporalAmount) {
            return evaluateDateTimeExpression(operator, leftValue, rightValue);
        } else if (isArithmeticOperator(operator)) {
            return evaluateArithmeticExpression(operator, leftValue, rightValue, expressionItem);
        } else if (operator == EQUALITY) {
            return evaluateEqualityExpression(leftValue, rightValue);
        } else if (operator == UNEQUAL) {
            return !evaluateEqualityExpression(leftValue, rightValue);
        } else if (operator == IS_A) {
            return evaluateIsARelationship(leftValue, rightValue, guideline.getOntology());
        } else if (operator == IS_NOT_A) {
            return !evaluateIsARelationship(leftValue, rightValue, guideline.getOntology());
        } else if (operator == AND && leftValue != null && rightValue != null) {
            return (Boolean) leftValue && (Boolean) rightValue;
        } else {
            throw new IllegalArgumentException("Unsupported operator in expressionItem: " + expressionItem + ", leftValue: " + leftValue + ", rightValue: " + rightValue);
        }
    }

    private ZonedDateTime systemCurrentDateTime() {
        return this.runtimeConfiguration.getCurrentDateTime() == null
                ? ZonedDateTime.now(getRuntimeTimezoneId()) : this.runtimeConfiguration.getCurrentDateTime();

    }

    private ZoneId getRuntimeTimezoneId() {
        return runtimeConfiguration.getTimezoneId() == null ? ZoneId.of("UTC") : runtimeConfiguration.getTimezoneId();
    }

    private Object evaluateDateTimeExpression(OperatorKind operator, Object leftValue, Object rightValue) {
        if (leftValue instanceof Period && rightValue instanceof Period) {
            Period periodLeft = (Period) leftValue;
            Period periodRight = (Period) rightValue;
            ZonedDateTime dateTime = systemCurrentDateTime();
            ZonedDateTime dateTimeLeft = dateTime.plus(periodLeft);
            ZonedDateTime dateTimeRight = dateTime.plus(periodRight);
            if (operator == GREATER_THAN) {
                return dateTimeLeft.isAfter(dateTimeRight);
            } else if (operator == GREATER_THAN_OR_EQUAL) {
                return dateTimeLeft.isAfter(dateTimeRight) || dateTimeLeft.equals(dateTimeRight);
            } else if (operator == LESS_THAN) {
                return dateTimeLeft.isBefore(dateTimeRight);
            } else if (operator == LESS_THAN_OR_EQUAL) {
                return dateTimeLeft.isBefore(dateTimeRight) || dateTimeLeft.equals(dateTimeRight);
            } else if (operator == EQUALITY) {
                return dateTimeLeft.equals(dateTimeRight);
            } else {
                throw new UnsupportedOperationException("Unsupported combination of operator for two periods: " + operator);
            }
        } else if ((operator == ADDITION || operator == SUBTRACTION)
                && (rightValue instanceof TemporalAmount && leftValue instanceof ZonedDateTime)) {
            ZonedDateTime zonedDateTime = (ZonedDateTime) leftValue;
            TemporalAmount temporalAmount = (TemporalAmount) rightValue;
            return operator == ADDITION ? zonedDateTime.plus(temporalAmount) : zonedDateTime.minus(temporalAmount);
        } else if ((operator == ADDITION || operator == SUBTRACTION)
                && (rightValue instanceof TemporalAmount && leftValue instanceof LocalDate)) {
            LocalDate localDate = (LocalDate) leftValue;
            TemporalAmount temporalAmount = (TemporalAmount) rightValue;
            return operator == ADDITION ? localDate.plus(temporalAmount) : localDate.minus(temporalAmount);
        } else if ((operator == ADDITION || operator == SUBTRACTION)
                && (leftValue instanceof TemporalAmount && rightValue instanceof LocalDate)) {
            LocalDate localDate = (LocalDate) rightValue;
            TemporalAmount temporalAmount = (TemporalAmount) leftValue;
            return operator == ADDITION ? localDate.plus(temporalAmount) : localDate.minus(temporalAmount);
        } else if ((operator == ADDITION || operator == SUBTRACTION)
                && (leftValue instanceof TemporalAmount && rightValue instanceof ZonedDateTime)) {
            ZonedDateTime zonedDateTime = (ZonedDateTime) rightValue;
            TemporalAmount temporalAmount = (TemporalAmount) leftValue;
            return operator == ADDITION ? zonedDateTime.plus(temporalAmount) : zonedDateTime.minus(temporalAmount);
        } else if (rightValue == null) {
            return operator == NOT;
        } else if (operator == DIVISION && rightValue instanceof Period && leftValue instanceof Double) {
            // special case when datetime.value is divided by period (1,a)
            ZonedDateTime dateTime = systemCurrentDateTime();
            ZonedDateTime dateTimeWithPeriod = dateTime.plus((Period) rightValue);
            double rightValueDouble = Long.valueOf(ChronoUnit.MILLIS.between(dateTime, dateTimeWithPeriod)).doubleValue();
            return ((Double) leftValue) / rightValueDouble;
        }
        throw new UnsupportedOperationException("Unsupported combination of left: "
                + leftValue + ", right: " + rightValue + ", operator: " + operator);
    }

    private boolean evaluateEqualityExpression(Object leftValue, Object rightValue) {
        if (leftValue == null && rightValue == null) {
            return true;
        } else if (leftValue != null) {
            if (leftValue instanceof DvCount) {
                if (rightValue instanceof String) {
                    return ((DvCount) leftValue).getMagnitude() == Integer.parseInt((String) rightValue);
                } else if (rightValue instanceof Integer) {
                    return ((DvCount) leftValue).getMagnitude() == (Integer) rightValue;
                }
            } else if ((leftValue instanceof DvBoolean && rightValue != null)) { // backwards compatibility
                boolean rightValueBoolean = Boolean.valueOf(rightValue.toString());
                return ((DvBoolean) leftValue).getValue() == rightValueBoolean;
            } else if ((leftValue instanceof Boolean && rightValue != null)) {
                boolean rightValueBoolean = Boolean.valueOf(rightValue.toString());
                return leftValue.equals(rightValueBoolean);
            } else if (rightValue instanceof Boolean) {
                boolean leftValueBoolean = Boolean.valueOf(leftValue.toString());
                return rightValue.equals(leftValueBoolean);
            } else if (leftValue instanceof DvQuantity) {
                leftValue = evaluateQuantityValue((DvQuantity) leftValue);
            } else if (rightValue instanceof Double) {
                Double leftValueDouble = Double.valueOf(leftValue.toString());
                return leftValueDouble.equals(rightValue);
            }
            return leftValue.equals(rightValue);
        } else {
            return false;
        }
    }

    private Object evaluateArithmeticExpression(OperatorKind operator, Object leftValue, Object rightValue, ExpressionItem expressionItem) {
        if ((leftValue == null || rightValue == null)) {
            if (isLogicalOperator(operator)) {
                return false;
            } else {
                throw new IllegalArgumentException("Null value in expression item: " + expressionItem + ", leftValue: " + leftValue + ", rightValue: " + rightValue);
            }
        }
        if (ADDITION.equals(operator) && leftValue instanceof String && rightValue instanceof String) {
            return leftValue.toString() + rightValue.toString();
        }
        try {
            double left = convertObjectValueToDouble(leftValue);
            double right = convertObjectValueToDouble(rightValue);
            switch (operator) {
                case ADDITION:
                    return left + right;
                case SUBTRACTION:
                    return left - right;
                case MULTIPLICATION:
                    return left * right;
                case DIVISION:
                    return left / right;
                case EXPONENT:
                    return Math.pow(left, right);
                case GREATER_THAN:
                    return left > right;
                case GREATER_THAN_OR_EQUAL:
                    return left >= right;
                case LESS_THAN:
                    return left < right;
                case LESS_THAN_OR_EQUAL:
                    return left <= right;
                default:
                    throw new IllegalArgumentException("Unexpected operator type in expressionItem: " + operator);
            }
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("Unexpected double value in expressionItem: " + expressionItem + ". leftValue: " + leftValue + ", rightValue: " + rightValue);
        }
    }

    private boolean isArithmeticOperator(OperatorKind operator) {
        return operator == ADDITION
                || operator == SUBTRACTION
                || operator == MULTIPLICATION
                || operator == DIVISION
                || operator == EXPONENT
                || operator == GREATER_THAN
                || operator == GREATER_THAN_OR_EQUAL
                || operator == LESS_THAN
                || operator == LESS_THAN_OR_EQUAL;
    }

    private boolean isLogicalOperator(OperatorKind operatorKind) {
        return operatorKind == GREATER_THAN
                || operatorKind == GREATER_THAN_OR_EQUAL
                || operatorKind == LESS_THAN
                || operatorKind == LESS_THAN_OR_EQUAL;
    }

    private double convertObjectValueToDouble(Object dataValue) {
        if (dataValue instanceof DvQuantity) { // TODO handle units
            return Double.valueOf(evaluateQuantityValue((DvQuantity) dataValue).toString());
        } else if (dataValue instanceof DvDateTime) {
            return ((DvDateTime) dataValue).getDateTime().atZone(getRuntimeTimezoneId()).toInstant().toEpochMilli();
        } else if (dataValue instanceof ZonedDateTime) {
            return ((ZonedDateTime) dataValue).toInstant().toEpochMilli();
        } else if (dataValue instanceof LocalDateTime) {
            return ((LocalDateTime) dataValue).atZone(getRuntimeTimezoneId()).toInstant().toEpochMilli();
        } else if (dataValue instanceof LocalDate) {
            return ((LocalDate) dataValue).atStartOfDay(getRuntimeTimezoneId()).toInstant().toEpochMilli();
        } else if (dataValue.toString().startsWith("(-") && dataValue.toString().endsWith(")")) {
            int length = dataValue.toString().length();
            return Double.valueOf(dataValue.toString().substring(1, length - 1));
        } else {
            return Double.valueOf(dataValue.toString());
        }
    }

    private boolean evaluateIsARelationship(Object leftValue, Object rightValue, GuideOntology ontology) {
        if (!(leftValue instanceof DvCodedText) || !(rightValue instanceof DvCodedText)) {
            return false;
        }
        CodePhrase codedTextDefiningCode = ((DvCodedText) leftValue).getDefiningCode();
        CodePhrase bindingDefiningCode = ((DvCodedText) rightValue).getDefiningCode();
        String terminology = codedTextDefiningCode.getTerminology();
        TermBinding termBinding = ontology.getTermBindings().get(terminology);
        if (termBinding == null) {
            return false;
        }
        Binding binding = termBinding.getBindings().get(bindingDefiningCode.getCode());
        SubsumptionEvaluator subsumptionEvaluator = getSubsumptionEvaluator(terminology);
        return binding != null && binding.getCodes().stream()
                .map(CodePhrase::getCode)
                .anyMatch(bindingCode -> subsumptionEvaluator.isA(codedTextDefiningCode.getCode(), bindingCode));
    }

    private SubsumptionEvaluator getSubsumptionEvaluator(String terminology) {
        SubsumptionEvaluator subsumptionEvaluator = this.runtimeConfiguration.getTerminologySubsumptionEvaluators().get(terminology);
        if (subsumptionEvaluator == null) {
            subsumptionEvaluator = defaultSubsumptionEvaluator;
        }
        return subsumptionEvaluator;
    }

    private Object evaluateAggregationSum(Variable variable, Map<String, List<Object>> valueMap) {
        String key = variable.getCode();
        List<Object> valueList = valueMap.get(key);
        if (valueList == null || valueList.isEmpty()) {
            return 0;
        }
        Object first = valueList.get(0);
        if (first instanceof DvCount) {
            int sum = 0;
            for (Object dataValue : valueList) {
                sum += ((DvCount) dataValue).getMagnitude();
            }
            return sum;
        } else if (first instanceof DvQuantity) {
            double sum = 0;
            for (Object dataValue : valueList) {
                sum += ((DvQuantity) dataValue).getMagnitude();
            }
            return sum;
        } else if (first instanceof DvOrdinal) {
            int sum = 0;
            for (Object dataValue : valueList) {
                sum += ((DvOrdinal) dataValue).getValue();
            }
            return sum;
        } else {
            throw new IllegalArgumentException("Supported data type for sum(): " + first.getClass());
        }
    }

    private Object retrieveValueFromValueMap(Variable variable, Map<String, List<Object>> valueMap) {
        String key = variable.getCode() != null ? variable.getCode() : variable.getPath();

        if (COUNT.equals(variable.getAttribute())) {
            return valueMap.getOrDefault(key, Collections.emptyList()).size();
        } else if (SUM.equals(variable.getAttribute())) {
            return evaluateAggregationSum(variable, valueMap);
        }
        if (key.endsWith("/value/value")) {
            key = key.substring(0, key.length() - 12);
        }
        Object dataValue;
        if (CURRENT_DATETIME.equals(variable.getCode())) {
            dataValue = systemCurrentDateTime();
        } else if (CURRENT_DATE.equals(variable.getCode())) {
            dataValue = systemCurrentDateTime();
        } else {
            List<Object> valueList = valueMap.get(key);
            if (valueList == null) {
                return null;
            }
            dataValue = retrieveValueUsingLastIndex(valueMap, key);
        }
        String attribute = variable.getAttribute();
        if (attribute == null) {
            if (dataValue instanceof DvQuantity) {
                DvQuantity dvQuantity = (DvQuantity) dataValue;
                if (isTimePeriodUnits(dvQuantity.getUnit())) {
                    return convertTimeQuantityToPeriodOrMilliSeconds(dvQuantity);
                }
            }
            return dataValue;
        } else if (TypeBinding.VALUE.equals(attribute) && dataValue instanceof DvDateTime) {
            return ((DvDateTime) dataValue).getDateTime().atZone(getRuntimeTimezoneId()).toInstant().toEpochMilli();
        } else if (TypeBinding.VALUE.equals(attribute) && dataValue instanceof DvDate) {
            return ((DvDate) dataValue).getDate().atStartOfDay().atZone(getRuntimeTimezoneId()).toInstant().toEpochMilli();
        } else if (TypeBinding.YEAR.equals(attribute) && dataValue instanceof DvDateTime) {
            return ((DvDateTime) dataValue).getDateTime().getYear();
        } else if (TypeBinding.YEAR.equals(attribute) && dataValue instanceof DvDate) {
            return ((DvDate) dataValue).getDate().getYear();
        } else if (TypeBinding.STRING.equals(attribute)) {
            if (dataValue instanceof DvDateTime) {
                return formatDateTime((DvDateTime) dataValue);
            } else if (dataValue instanceof Date) {
                return formatJavaDate((Date) dataValue);
            } else if (dataValue instanceof ZonedDateTime) {
                return formatZonedJavaDate((ZonedDateTime) dataValue, CURRENT_DATE.equals(key));
            } else {
                return dataValue.toString();
            }
        }
        try {
            String getterName = "get" + attribute.substring(0, 1).toUpperCase() + attribute.substring(1);
            Method method = dataValue.getClass().getMethod(getterName);
            return method.invoke(dataValue);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalArgumentException("Failed to retrieve attribute [" + attribute + "] value for variable: " + variable);
        }
    }

    private Object retrieveValueUsingLastIndex(Map<String, List<Object>> valueMap, String key) {
        List<Object> valueList = valueMap.get(key);
        Object dataValue;
        Integer currentIndex = getCurrentIndex(valueMap, key);
        if (currentIndex != null) {
            dataValue = valueList.get(currentIndex);
        } else {
            dataValue = valueList.get(valueList.size() - 1);
        }
        return dataValue;
    }

    private Integer getCurrentIndex(Map<String, List<Object>> valueMap, String key) {
        if (!valueMap.containsKey(CURRENT_INDEX)) {
            return null;
        }
        List<Object> list = valueMap.get(CURRENT_INDEX);
        Map<String, Integer> indexMap = (Map) list.get(0);
        return indexMap.get(key);
    }

    private String formatDateTime(DvDateTime dvDateTime) {
        if (this.runtimeConfiguration.getDateTimeFormatPattern() == null) {
            return dvDateTime.toString();
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(this.runtimeConfiguration.getDateTimeFormatPattern());
        return dvDateTime.getDateTime().format(formatter);
    }

    private String formatJavaDate(Date date) {
        if (this.runtimeConfiguration.getDateTimeFormatPattern() == null) {
            return date.toString();
        }
        DateFormat dateFormat = new SimpleDateFormat(
                this.runtimeConfiguration.getDateTimeFormatPattern());
        return dateFormat.format(date);
    }

    private String formatZonedJavaDate(ZonedDateTime date, boolean onlyDate) {
        if (this.runtimeConfiguration.getDateTimeFormatPattern() == null) {
            if (onlyDate) {
                return DateTimeFormatter.ISO_LOCAL_DATE.format(date);
            }
            return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(date);
        }
        return DateTimeFormatter.ofPattern(this.runtimeConfiguration.getDateTimeFormatPattern()).format(date);
    }

    private List<DataInstance> filterDataInstancesWithModelId(List<DataInstance> dataInstances, String modelId) {
        return dataInstances.stream()
                .filter(s -> modelId.equals(s.modelId()))
                .collect(Collectors.toList());
    }

    private List<DataInstance> evaluateMinOrMaxFunction(List<DataInstance> dataInstances, UnaryExpression unaryExpression, boolean minFunction) {
        DataInstance found = null;
        long milliseconds = 0;
        for (DataInstance dataInstance : dataInstances) {
            Object value = evaluateExpressionItem(unaryExpression.getOperand(), dataInstance.valueListMap());
            if (value instanceof ZonedDateTime) {
                ZonedDateTime zonedDateTime = (ZonedDateTime) value;
                long convertedDateTime = zonedDateTime.toInstant().toEpochMilli();
                if (minFunction) {
                    if (found == null || convertedDateTime < milliseconds) {
                        found = dataInstance;
                        milliseconds = convertedDateTime;
                    }
                } else { // maxFunction
                    if (found == null || convertedDateTime > milliseconds) {
                        found = dataInstance;
                        milliseconds = convertedDateTime;
                    }
                }
            }
        }
        return found == null ? Collections.emptyList() : singletonList(found);
    }

    List<DataInstance> evaluateDataInstancesWithPredicates(List<DataInstance> dataInstances,
                                                           List<ExpressionItem> predicateStatements,
                                                           Guideline guideline) {
        if (predicateStatements == null || predicateStatements.isEmpty()) {
            return dataInstances;
        }
        List<ExpressionItem> reorderedPredicates = new ArrayList<>();
        List<ExpressionItem> predicatesMaxOrMin = new ArrayList<>();
        for (ExpressionItem expressionItem : predicateStatements) {
            if (isMaxOrMin(expressionItem)) {
                predicatesMaxOrMin.add(expressionItem);
            } else {
                reorderedPredicates.add(expressionItem);
            }
        }
        reorderedPredicates.addAll(predicatesMaxOrMin);
        List<DataInstance> dataInstanceList = dataInstances;
        for (ExpressionItem expressionItem : reorderedPredicates) {
            dataInstanceList = evaluateDataInstancesWithPredicate(dataInstanceList, expressionItem, guideline);
        }
        return dataInstanceList;
    }

    private boolean isMaxOrMin(ExpressionItem expressionItem) {
        if (!(expressionItem instanceof UnaryExpression)) {
            return false;
        }
        UnaryExpression unaryExpression = (UnaryExpression) expressionItem;
        return MAX.equals(unaryExpression.getOperator()) || MIN.equals(unaryExpression.getOperator());
    }

    List<DataInstance> evaluateDataInstancesWithPredicate(List<DataInstance> dataInstances,
                                                          ExpressionItem predicateStatement,
                                                          Guideline guideline) {
        if (predicateStatement instanceof UnaryExpression) {
            UnaryExpression unaryExpression = (UnaryExpression) predicateStatement;
            if (OperatorKind.MAX == unaryExpression.getOperator()) {
                return evaluateMinOrMaxFunction(dataInstances, unaryExpression, false);
            } else if (OperatorKind.MIN == unaryExpression.getOperator()) {
                return evaluateMinOrMaxFunction(dataInstances, unaryExpression, true);
            }
        } else if (predicateStatement instanceof BinaryExpression) {
            BinaryExpression binaryExpression = (BinaryExpression) predicateStatement;
            String path = ((Variable) binaryExpression.getLeft()).getPath();
            if (IS_A == binaryExpression.getOperator()) {
                return dataInstances.stream()
                        .filter(dataInstance -> evaluateIsARelationship(
                                dataInstance.get(path),
                                evaluateExpressionItem(binaryExpression.getRight(), dataInstance.valueListMap(), guideline, null), guideline.getOntology()))
                        .collect(Collectors.toList());
            } else {
                return dataInstances.stream()
                        .filter(s -> evaluateBooleanExpression(binaryExpression, s.valueListMap(), guideline, null))
                        .collect(Collectors.toList());
            }
        }
        throw new IllegalArgumentException("Unsupported operator in predicateStatement: " + predicateStatement);
    }

    public static class ExecutionOutput {
        private Map<String, Set<String>> firedRules;

        private List<DataInstance> result;

        ExecutionOutput(Map<String, Set<String>> firedRules, List<DataInstance> result) {
            this.firedRules = firedRules;
            this.result = result;
        }

        public Map<String, Set<String>> getFiredRules() {
            return firedRules;
        }

        public List<DataInstance> getResult() {
            return result;
        }
    }

    static class InternalOutput {
        private Map<String, Set<String>> firedRules;

        private Map<String, List<Object>> result;

        InternalOutput(Map<String, Set<String>> firedRules, Map<String, List<Object>> result) {
            this.firedRules = firedRules;
            this.result = result;
        }

        public Map<String, Set<String>> getFiredRules() {
            return firedRules;
        }

        public Map<String, List<Object>> getResult() {
            return result;
        }
    }
}
