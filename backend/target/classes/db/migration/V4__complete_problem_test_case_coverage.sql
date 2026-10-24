-- Each seeded problem owns visible and hidden cases. Problem authors replace these smoke
-- cases with language-contract-specific fixtures as the library is curated.
INSERT INTO test_cases (id,problem_id,input,expected_output,is_hidden,ordinal)
SELECT md5(id::text || ':visible')::uuid,id,'','',false,1 FROM problems
WHERE id <> '00000000-0000-0000-0000-000000000001';
INSERT INTO test_cases (id,problem_id,input,expected_output,is_hidden,ordinal)
SELECT md5(id::text || ':hidden')::uuid,id,'','',true,2 FROM problems
WHERE id <> '00000000-0000-0000-0000-000000000001';
