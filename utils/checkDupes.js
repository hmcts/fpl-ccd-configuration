const fs = require('fs');
const PATH = 'ccd-definition/AuthorisationCaseField/CareSupervision';
let set = new Set();

let dupes = new Set();

function doCheck(field, role) {
  let permStr = field + ' : ' + role;
  if (set.has(permStr)) {
    dupes.add(permStr);
    console.log(permStr);
  } else {
    set.add(permStr);
  }
}

fs.readdirSync(PATH).forEach(file => {
  let permFile = JSON.parse(fs.readFileSync(PATH + '/' + file));

  permFile.forEach(perm => {
    if (perm['UserRoles'] !== undefined) {
      perm['UserRoles'].forEach(role => {
        doCheck(perm['CaseFieldID'], role);
      });
    } else if (perm['UserRole'] !== undefined) {
      doCheck(perm['CaseFieldID'], perm['UserRole']);
    } else {
      perm['AccessControl'].forEach(ac => {
        ac['UserRoles'].forEach(role => {
          doCheck(perm['CaseFieldID'], role);
        });
      });
    }
  });
});

if (dupes.size > 0) {
  console.log('Permissions dupes detected, ABORTING');
  process.exit(1);
} else {
  console.log('No permissions dupes detected');
}

