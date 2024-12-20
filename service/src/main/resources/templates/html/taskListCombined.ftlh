<div class='width-50'>
  <br/>
  <#if applicationType??>
    <div class='govuk-tag govuk-tag--blue'>
        ${applicationType} Application
        <br/>
        <i>${applicationType} Cais</i>
    </div>
  </#if>

  <#list sections as section>
    <br/>

    ## ${section.getNameInLang(false)}

    ### _${section.getNameInLang(true)}_

    <#if section.getHintInLang(false).isPresent()>
      <span class='govuk-hint govuk-!-font-size-14'>
        ${section.getHintInLang(false).get()}

        <i class="govuk-hint govuk-!-font-size-12">${section.getHintInLang(true).get()}</i>

      </span>
    </#if>

    <#if section.getInfoInLang(false).isPresent()>
      <div class='panel panel-border-wide govuk-!-font-size-16'>
        ${section.getInfoInLang(false).get()}

        <i class="govuk-!-font-size-14">${section.getInfoInLang(true).get()}</i>
      </div>

    </#if>

    <hr class='govuk-!-margin-top-3 govuk-!-margin-bottom-2'/>

    <#list section.tasks as task>
      <span>

        <#if task.getState() == "NOT_AVAILABLE">
          <a>${task.getEvent().getNameInLang(false)}</a>
          <img align='right' height='25px' src='https://raw.githubusercontent.com/hmcts/fpl-ccd-configuration/master/resources/cannot-send-yet.png' title='Cannot send yet' alt='Cannot send yet'/>

          <a class="govuk-!-font-size-14"><i>${task.getEvent().getNameInLang(true)}</i></a>
        <#else>
          <a href='/case/${jurisdiction}/${caseType}/${caseId}/trigger/${task.getEvent().getId()}'>${task.getEvent().getNameInLang(false)}</a>
            <#if task.getState() == "IN_PROGRESS">
              <img align='right' height='25px' src='https://raw.githubusercontent.com/hmcts/fpl-ccd-configuration/master/resources/in-progress.png' title='In progress' alt='In progress'/>
            </#if>
            <#if task.getState() == "COMPLETED">
              <img align='right' height='25px' src='https://raw.githubusercontent.com/hmcts/fpl-ccd-configuration/master/resources/information-added.png' title='Information added' alt='Information added'/>
            </#if>
            <#if task.getState() == "COMPLETED_FINISHED">
              <img align='right' height='25px' src='https://raw.githubusercontent.com/hmcts/fpl-ccd-configuration/master/resources/finished.png' title='Finished' alt='Finished'/>
            </#if>

          <a href='/case/${jurisdiction}/${caseType}/${caseId}/trigger/${task.getEvent().getId()}'><i class="govuk-!-font-size-14">${task.getEvent().getNameInLang(true)}</i></a>
        </#if>
      </span>

      <#if task.getHintInLang(false).isPresent()>
        <span class='govuk-hint govuk-!-font-size-14'>
          ${task.getHintInLang(false).get()}
          <i class='govuk-hint govuk-!-font-size-12'>${task.getHintInLang(true).get()}</i>
        </span>
      </#if>
      <hr class='govuk-!-margin-top-3 govuk-!-margin-bottom-2'/>
    </#list>
  </#list>
</div>
<details class='govuk-details'>
  <summary class='govuk-details__summary'>
    <span class='govuk-details__summary-text'>
      Why can't I submit my application?

      <i class="govuk-!-font-size-14">Pam na allaf gyflwyno fy nghais?</i>
    </span>
  </summary>
  <div class='govuk-details__text'>
    <#list taskErrors as taskError>
      <#list taskError.errors as error>
        ${error?keep_before("|")} in <a href="/case/${jurisdiction}/${caseType}/${caseId}/trigger/${taskError.getEvent().getId()}">${taskError.getEvent().getNameInLang(false)}</a>

        <i class="govuk-!-font-size-14">${error?keep_after("|")} yn <a href="/case/${jurisdiction}/${caseType}/${caseId}/trigger/${taskError.getEvent().getId()}">${taskError.getEvent().getNameInLang(true)}</a></i>

      </#list>
    </#list>
  </div>
</details>
