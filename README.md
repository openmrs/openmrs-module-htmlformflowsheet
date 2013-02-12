openmrs-module-htmlformflowsheet
================================

Back end functionality to do a "patient chart"-style workflow (as opposed to form-based workflow)


<!--    
       This is a template for what you can put into the role-based homepage module
       to create htmlformflowsheet-based apps.  This is dependent on the global property 
       htmlformflowsheet.programConfigurationMap
       which must be set correctly for this to work.
-->

<!-- Set whatever title you want -->
<h3>Non Communicable Disease (NCD) Homepage</h3><br/>

<!--  Here, you must  set two properties.  

       First, where you see program=5,6 change these values to all of the programs
       in the global property htmlformflowsheet.programConfigurationMap for which you want this role to be able to 
       access the homepage.  You *can't* add properties here that aren't pre-configured in the global property - they 
       won't work.

       Second, where you see createPatientFormId=16, change this value to the formId (found under Admin/Manage
       Forms) of the create patient htmlform that you'd like to use for this program.  If you do not wish for this role
       to be able to use the 'create patient' dialogues, you can leave this parameter out, but you will see a benign 
       error message instead.
-->

     <openmrs:portlet 
          id="htmlformflowsheetFindPatient" 
          url="htmlformflowsheetFindPatient" 
          parameters="size=full|postURL=|showIncludeVoided=false|viewType=shortEdit|program=5,6|createPatientFormId=16" 
          moduleId="htmlformflowsheet"
     />

