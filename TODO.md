# List of the tasks to be done:
- Add the rule: SELECT DISTINCT assays.assay_id, assays.description, assays.chembl_id FROM assays, activities, target_dictionary, molecule_dictionary WHERE assays.tid = target_dictionary.tid AND target_dictionary.target_type = 'SINGLE PROTEIN' AND assays.assay_id = activities.assay_id AND activities.molregno = molecule_dictionary.molregno AND molecule_dictionary.molecule_type = 'Small molecule';
- Select the assays involving small molecules on single protein targets
- 5
- protein-small molecule interaction assay
- As now, it takes way too much time to be executed.
