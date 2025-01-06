<#ftl auto_esc=false>
<div class='width-50'>
<br/>
<#if applicationType??>
<div class='govuk-tag govuk-tag--blue'><#if welsh>${applicationType} Cais<#else>${applicationType} Application</#if></div>
<br/>
</#if>

<#list sections as section>

  <@compress single_line=true> <#-- compress heading to a single line so the markdown is rendered correctly -->
|
  <h2 class="govuk-heading-m govuk-!-margin-0">${section.getNameInLang(welsh)}</h2>
  <#if section.getHintInLang(welsh).isPresent()>
    <div class='govuk-hint govuk-!-font-size-16'>${section.getHintInLang(welsh).get()}</div>
  </#if>
  <#if section.getInfoInLang(welsh).isPresent()>
    <div class='govuk-inset-text govuk-!-margin-0 govuk-!-font-size-16'>${section.getInfoInLang(welsh).get()}</div>
  </#if>
| |
</@compress>

| :--- | ---: |
<#list section.tasks as task>
<@compress single_line=true> <#-- compress each task to a single line so the markdown table row is rendered correctly -->
  | <#-- Link column -->
  <#if task.getState() == "NOT_AVAILABLE">
    <a>${task.getEvent().getNameInLang(welsh)}</a>
  <#else>
    <a href='/case/${jurisdiction}/${caseType}/${caseId}/trigger/${task.getEvent().getId()}'>${task.getEvent().getNameInLang(welsh)}</a>
  </#if>
  <#if task.getHintInLang(welsh).isPresent()>
    <div class='govuk-hint govuk-!-font-size-14'>${task.getHintInLang(welsh).get()}</div>
  </#if>
  | <#-- Status tag column -->
  <div class="govuk-!-display-block govuk-!-text-align-right">
    <#if task.getState() == "IN_PROGRESS">
      <span class='govuk-tag govuk-tag--blue'><#if welsh>Ar Waith<#else>In Progress</#if></span>
    <#elseif task.getState() == "COMPLETED_FINISHED" || task.getState() == "COMPLETED">
      <span class='govuk-tag govuk-tag--green'><#if welsh>Wedi'i gwblhau<#else>Completed</#if></span>
    <#elseif task.getState() == "NOT_AVAILABLE">
      <span class='govuk-tag govuk-tag--grey'><#if welsh>Methu cychwyn eto<#else>Cannot start yet</#if></span>
    </#if>
  </div>
  | <#-- End row -->
</@compress>

</#list>

</#list>
</div>

<#if taskErrors?? && (taskErrors?size > 0)>
<details class='govuk-details'>
  <summary class='govuk-details__summary'>
    <span class='govuk-details__summary-text'>
      <#if welsh>
        Pam na allaf gyflwyno fy nghais?
      <#else>
        Why can't I submit my application?
      </#if>
    </span>
  </summary>
  <div class='govuk-details__text'>
    <#list taskErrors as taskError>
      <#list taskError.errors as error>

        <@compress single_line=true>
          <#if welsh>
            ${error?keep_after("|")}
            <#if taskError.getEvent().getNameInLang(welsh)?matches("^(m|M).+")>
              ym
            <#else>
              yn
            </#if>
          <#else>
            ${error?keep_before("|")} in
          </#if>
          <a href="/case/${jurisdiction}/${caseType}/${caseId}/trigger/${taskError.getEvent().getId()}">${taskError.getEvent().getNameInLang(welsh)}</a>
        </@compress>

      </#list>
    </#list>
  </div>
</details>
</#if>
