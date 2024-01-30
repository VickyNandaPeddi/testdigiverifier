import { Component, OnInit } from '@angular/core';
import { FormGroup, FormControl, FormBuilder, Validators, FormArray } from '@angular/forms';
import { CustomerService } from '../../services/customer.service';
import Swal from 'sweetalert2';
import { ActivatedRoute, Router } from '@angular/router';
import {ModalDismissReasons, NgbModal} from '@ng-bootstrap/ng-bootstrap';
import { AuthenticationService } from 'src/app/services/authentication.service';

@Component({
  selector: 'app-vendor-initiate',
  templateUrl: './vendor-initiate.component.html',
  styleUrls: ['./vendor-initiate.component.scss']
})


export class VendorInitiateComponent implements OnInit {
    pageTitle = 'Initiate Vendor Check';
    vendoruser:any
    userID: any;
    getbgv: any=[];
    getBillValues: any=[];
    VendorData_stat:boolean=false;
    getVendorID: any=[];
    candidateId: any;
    sourceid:any;
    sourceName:any;
    vendorid:any;
    getCustomerBillData:any;
    closeModal: string | undefined;
    Employments: Boolean=false;
    education: Boolean=false;
    GlobalDatabasecheck: Boolean=false;
    Address: Boolean=false;
    IDItems: Boolean=false;
    crimnal: Boolean=false;
    DrugTest: Boolean=false;
    PhysicalVisit: Boolean=false;
    public proofDocumentNew: any = File;
    public empexirdocument: any = File;
    vendorchecksupload: any=[];
    agentAttributeListForm: any[] = [];
    AgentAttributeCheck: any=[];
    crimnalGlobalAgentAttributeCheckMapped:any[]=[];
    educationAgentAttributeCheckMapped:any[]=[];
    showMessage:any
    idItemsAgentAttributeCheckMapped:any[]=[];
    getVenorcheckStatus: any[] = [];
    vendorCheckStatusMasterId: any;


    vendorlist = new FormGroup({
      // organizationIds: new FormControl('', Validators.required),
      vendorId: new FormControl(''),
      userId: new FormControl('', Validators.required),
      sourceId: new FormControl('', Validators.required),
      candidateId: new FormControl(''),
      documentName: new FormControl('', Validators.required),
      document: new FormControl('', Validators.required),
    });
    patchUserValues() {
      this.vendorlist.patchValue({
        sourceId: this.tmp,
        candidateId: this.candidateId,
      });
    }

    formEditEdu = new FormGroup({
      documentname: new FormControl('', Validators.required),
      vendorId: new FormControl(''),
      sourceId: new FormControl('', Validators.required),
      candidateId: new FormControl(''),
      value: new FormControl(''),
      // fileInput: new FormControl('',Validators.required),
      vendorCheckStatusMasterId: new FormControl('')
    });
    patcheduValues() {
      this.formEditEdu.patchValue({
        sourceId: this.sourceid,
        candidateId: this.candidateId,
        vendorId:this.vendorid
      });

    }

    foremployements = new FormGroup({
      candidateName: new FormControl('', Validators.required),
      documentname: new FormControl('', Validators.required),
      vendorId: new FormControl(''),
      sourceId: new FormControl(''),
      candidateId: new FormControl(''),
      // fileInput: new FormControl('',Validators.required),
      vendorCheckStatusMasterId: new FormControl('')
    });
    patcheduValuesemp() {
      this.foremployements.patchValue({
        sourceId: this.sourceid,
        candidateId: this.candidateId,
        vendorId:this.vendorid
      });

    }

    forAddressCrimnalGlobal = new FormGroup({
      // candidateName: new FormControl('', Validators.required),
      // dateOfBirth: new FormControl('', Validators.required),
      // contactNo: new FormControl('', Validators.required),
      // fatherName: new FormControl('', Validators.required),
      // address: new FormControl('', Validators.required),
      vendorId: new FormControl(''),
      sourceId: new FormControl(''),
      candidateId: new FormControl(''),
      value: new FormControl(""),
      vendorCheckStatusMasterId: new FormControl('')
    });
    patcheduValuesAddress() {
      this.forAddressCrimnalGlobal.patchValue({
        sourceId: this.sourceid,
        candidateId: this.candidateId,
        vendorId:this.vendorid
      });

    }

    forDrugTest = new FormGroup({
      candidateName: new FormControl('', Validators.required),
      documentname: new FormControl('', Validators.required),
      dateOfBirth: new FormControl('', Validators.required),
      contactNo: new FormControl('', Validators.required),
      fatherName: new FormControl('', Validators.required),
      address: new FormControl('', Validators.required),
      alternateContactNo: new FormControl('', Validators.required),
      typeOfPanel: new FormControl('', Validators.required),
      vendorId: new FormControl(''),
      sourceId: new FormControl(''),
      candidateId: new FormControl(''),
      // fileInput: new FormControl('',Validators.required),
      vendorCheckStatusMasterId: new FormControl('')
    });
    patcheduValuesDrugTest() {
      this.forDrugTest.patchValue({
        sourceId: this.sourceid,
        candidateId: this.candidateId,
        vendorId:this.vendorid
      });

    }

    formpassport = new FormGroup({
      candidateName: new FormControl('', Validators.required),
      documentname: new FormControl('', Validators.required),
      vendorId: new FormControl(''),
      sourceId: new FormControl('', Validators.required),
      candidateId: new FormControl(''),
      value: new FormControl(""),
      vendorCheckStatusMasterId: new FormControl('')

    });
    patchpassport() {
      this.formpassport.patchValue({
        sourceId: this.sourceid,
        candidateId: this.candidateId,
        vendorId:this.vendorid
      });

    }

    updateVendorForm = new FormGroup({
      vendorId: new FormControl(''),
      vendorcheckId: new FormControl('')
    })



    constructor( private customers:CustomerService, private router:ActivatedRoute, private fb: FormBuilder,private authService: AuthenticationService,
      private modalService: NgbModal, private navRouter: Router) {
      this.userID = this.router.snapshot.paramMap.get('userId');
      this.candidateId = this.router.snapshot.paramMap.get('candidateId');
      console.log(this.candidateId,"-----------------------------------")
      this.customers.getVendorCheckDetails(this.candidateId).subscribe((data: any)=>{

        this.vendorchecksupload=data.data;
        console.log(this.vendorchecksupload[0])
        if(this.getVendorID){
        for(var index in this.vendorchecksupload){
          for (var index1 in this.getVendorID){
          if(this.vendorchecksupload[index]["vendorId"]==this.getVendorID[index1]["userId"]){
            console.log(this.getVendorID[index1]["userFirstName"],"conuttt")
            this.vendorchecksupload[index]['username']=this.getVendorID[index1]["userFirstName"]
          }
          }
        }
      }
      console.log(this.vendorchecksupload)
      })
      if(authService.roleMatch(['ROLE_ADMIN']) || authService.roleMatch(['ROLE_AGENTHR']) || authService.roleMatch(['ROLE_AGENTSUPERVISOR'])){
        console.log(this.authService.getOrgID(),"------------------org id")
        this.customers.getVendorList(this.authService.getOrgID()).subscribe((data: any)=>{
          this.getVendorID=data.data;
          console.log(this.getVendorID,"-------------vendoy----------------");
          if(this.userID){
            for (var index in this.getVendorID){
                console.log(this.getVendorID[index]["userId"],"indexxxxxxxxxxxxxxxxxxxx");
                if(this.userID==this.getVendorID[index]["userId"]){
                  console.log(this.userID,"final")
                  this.vendoruser=this.getVendorID[index]["userFirstName"]
                  console.log(this.vendoruser,"finaluser")
                }

            }
        }
          // if(this.userID){
          //     if (this.userID== item.)
          // }
        });
        console.log(this.vendorlist.value,"-------------vend----------------");

      }
      let rportData = {
        'userId': this.authService.getuserId()
      }

      this.customers.getSources().subscribe((data: any)=>{
        this.getbgv=data.data;
        console.log(this.getbgv,"-------------getbgv----------------");
        this.getbgv.forEach((element:any) => {
          element.serviceId = '';
          element.ratePerItem = '';
         // element.ratePerItem = '';

        });
      });
    if(this.userID){

      this.customers.getAllVendorServices(this.userID).subscribe((data: any)=>{
        console.log("--------------------calling service--------------")
        this.getBillValues=data.data;
        console.log(this.getBillValues,"--------------------")
        if(this.getBillValues){
          this.getBillValues.forEach((element:any) => {
            // $(".billrpp"+element.source.sourceId).val(element.ratePerItem);
            // $(".billrpi"+element.source.sourceId).val(element.tatPerItem);
            // $(".billServiceId"+element.source.sourceId).val(element.userId);
            const billrpp = document.querySelector(".billrpp" + element.source?.sourceId) as HTMLInputElement;

            const billrpi = document.querySelector(".billrpi" + element.source?.sourceId) as HTMLInputElement;

            const billServiceId = document.querySelector(".billServiceId" + element.source?.sourceId) as HTMLInputElement;
            if (billrpp) {

              billrpp.value = element.ratePerItem;

              console.log("Report:", billrpp.value);

            }

            if (billrpi) {

              billrpi.value = element.tatPerItem;

              console.log("Item:", billrpi.value);

            }

            if (billServiceId) {

              billServiceId.value = element.userId;

              console.log("Service:", billServiceId.value);

            }

          });
        }

      });
    }

    }

    uploadGlobalCaseDetails(event:any) {
      const fileType = event.target.files[0].name.split('.').pop();
      const file = event.target.files[0];
      if(fileType == 'pdf' || fileType == 'PDF' || fileType == 'png' || fileType == 'PNG' || fileType == 'jpg' || fileType == 'JPG'){
        this.proofDocumentNew = file;
        this.empexirdocument = file;
      }else{
        event.target.value = null;
        Swal.fire({
          title: 'Please select .jpeg, .jpg, .png file type only.',
          icon: 'warning'
        });
      }
      }

    tmp: any=[];
    roleCboxes(e:any){
      var sid = e.target.id;
      console.log("checked======================",sid)
      if (e.target.checked) {
        // console.log("value************",value)
        this.tmp.push(sid);
      } else {
        this.tmp.splice($.inArray(sid, this.tmp),1);
      }
      console.log("checked==============================",this.tmp)
    }

    selectAll(e:any){
      if (e.target.checked) {
       // $(e.target).parent().siblings().find(".billServiceId").prop('checked', true);
        const checkboxes = e.target.parentNode?.parentNode?.querySelectorAll('.item input');
      if (checkboxes) {
        checkboxes.forEach((checkbox: any) => {
          checkbox.checked = true;
        });
      }
       var  iteminput = $('.item input');
        var arrNumber:any = [];
        $.each(iteminput,function(idx,elem){
          // var inputValues:any  = $(elem).val();
          // console.log(inputValues);
          arrNumber.push($(this).val());
        });

        this.tmp = arrNumber;
        console.log(this.tmp);
      } else {
       // $(e.target).parent().siblings().find(".billServiceId").prop('checked', false);
       const checkboxes = e.target.parentNode?.parentNode?.querySelectorAll('.item input');
      if (checkboxes) {
        checkboxes.forEach((checkbox: any) => {
          checkbox.checked = false;
        });
      }
      }

    }

    getvendorid(id:any){
      this.vendorid = id;
    }

    onKeyUp(){
     this.VendorData_stat = false;
    }
    ngOnInit(): void {
      this.customers.getVenorcheckStatus().subscribe((data: any) => {
        if(data.data) {
          this.getVenorcheckStatus = data.data.filter((temp: any)=> {
            if(temp.checkStatusCode != 'INPROGRESS') {
              return temp;
            }
          });
        }
        console.log(this.getVenorcheckStatus);
      });
    }

    getvendorcheckstatuss(event: any) {
      console.log("control entered with value: ", event.target.value);
      this.vendorCheckStatusMasterId = event.target.value;
    }


    submitEditEdu(formEditEdu: FormGroup) {

      this.patcheduValues()
      console.log("....................",this.formEditEdu.value)
      const formData = new FormData();
      const educationAttributeValues = this.agentAttributeListForm.reduce((obj, item) => {

        if (item.value === null || item.value.trim() === '') {
          return false; // Return false if any item.value is null or empty
        }

        obj[item.label] = item.value;

        return obj;

      }, {});

      if (educationAttributeValues === false) {
        console.error('Please enter values for all attributes');
        this.showMessage = "Please enter values for Mandatory Field";
      } else {
        console.log('CrimnalGlobalAttributeValues:', educationAttributeValues);
      }

      // this.educationAgentAttributeCheckMapped = {...this.formEditEdu.value, ...educationAttributeValues}
      const mergedData = {
        ...this.formEditEdu.value,
        ...educationAttributeValues,
      };
      formData.append('vendorchecks', JSON.stringify(mergedData));
      formData.append('file', this.proofDocumentNew);
      console.log(".........formData...........",formData)
      if(this.formEditEdu.valid && educationAttributeValues !== false){
        console.log(".........valid...........")
        this.customers.saveInitiateVendorChecks(formData).subscribe((result:any)=>{

          console.log(result,"=========result");
          if(result.outcome === true){
            Swal.fire({
              title: result.message,
              icon: 'success'
            }).then((result) => {
              if (result.isConfirmed) {
                window.location.reload();
              }
            });
          }else{
            Swal.fire({
              title: result.message,
              icon: 'warning'
            })
          }
        });
    }else{
      Swal.fire({
        title: 'Please enter the required details.',
        icon: 'warning'
      })
     }
    }

  submitEmploye(foremployements: FormGroup) {
    this.patcheduValuesemp()
    console.log("....................",this.foremployements.value)
    const formData = new FormData();
    formData.append('vendorchecks', JSON.stringify(this.foremployements.value));
    formData.append('file', this.empexirdocument);
    if(this.foremployements.valid){
      this.customers.saveInitiateVendorChecks(formData).subscribe((result:any)=>{
        console.log(result);
        if(result.outcome === true){
          Swal.fire({
            title: result.message,
            icon: 'success'
          }).then((result) => {
            if (result.isConfirmed) {
              window.location.reload();
            }
          });
        }else{
          Swal.fire({
            title: result.message,
            icon: 'warning'
          })
        }
      });
    }else{
      Swal.fire({
        title: 'Please enter the required details.',
        icon: 'warning'
      })
     }
    }

    submitAgentAttributes(forAddressCrimnalGlobal: FormGroup) {
      this.patcheduValuesAddress()

      console.log("....................",this.forAddressCrimnalGlobal.value)
      const formData = new FormData();

      console.log(this.agentAttributeListForm);
      const CrimnalGlobalAttributeValues = this.agentAttributeListForm.reduce((obj, item) => {

        if (item.value === null || item.value.trim() === '') {
          return false;
        }

         obj[item.label] = item.value;
        return obj;

      }, {});

      if (CrimnalGlobalAttributeValues === false) {
        console.error('Please enter values for all attributes');
        this.showMessage = "Please enter values for Mandatory Field";
      } else {
        console.log('CrimnalGlobalAttributeValues:', CrimnalGlobalAttributeValues);
      }

      //  delete agentAttributeValues.value
       this.crimnalGlobalAgentAttributeCheckMapped = {...this.forAddressCrimnalGlobal.value, ...CrimnalGlobalAttributeValues}

      // const finalValues = JSON.stringify(this.educationAgentAttributeCheckMapped);

      // console.log("finalValues",finalValues)

      console.log(" CrimnalGlobalAttributeValues:::", this.crimnalGlobalAgentAttributeCheckMapped);

      console.warn("CrimnalGlobalAttributeValues===>",CrimnalGlobalAttributeValues);

        const mergedData = {

            ...this.forAddressCrimnalGlobal.value,

          ...this.crimnalGlobalAgentAttributeCheckMapped,

        };

          //  formData.append('vendorchecks', JSON.stringify(this.forAddressCrimnalGlobal.value ))

          //  formData.append('vendorchecks', JSON.stringify(agentAttributeValues ))




        //  formData.append('vendorchecks', JSON.stringify(this.forAddressCrimnalGlobal.value ))
           formData.append('vendorchecks', JSON.stringify(mergedData));

         console.warn("mergedData++++++++++++++++++++",mergedData)

      // formData.append('vendorchecks', JSON.stringify(this.forAddressCrimnalGlobal.value));
      formData.append('file', this.proofDocumentNew);

      if(this.forAddressCrimnalGlobal.valid && CrimnalGlobalAttributeValues !== false){
        console.log(".........valid...........")
        this.customers.saveInitiateVendorChecks(formData).subscribe((result:any)=>{

          console.log(result);
          if(result.outcome === true){
            Swal.fire({
              title: result.message,
              icon: 'success'
            }).then((result) => {
              if (result.isConfirmed) {
                window.location.reload();
              }
            });
          }else{
            Swal.fire({
              title: result.message,
              icon: 'warning'
            })
          }
        });
      }else{
        Swal.fire({
          title: 'Please enter the required details.',
          icon: 'warning'
        })
       }
    }

    submitDrugTest(formEditEdu: FormGroup) {
      this.patcheduValuesDrugTest()
      console.log("....................",this.forDrugTest.value)
      const formData = new FormData();
      formData.append('vendorchecks', JSON.stringify(this.forDrugTest.value));
      formData.append('file', this.proofDocumentNew);
      return this.customers.saveInitiateVendorChecks(formData).subscribe((result:any)=>{

        console.log(result);
        if(result.outcome === true){
          Swal.fire({
            title: result.message,
            icon: 'success'
          }).then((result) => {
            if (result.isConfirmed) {
              window.location.reload();
            }
          });
        }else{
          Swal.fire({
            title: result.message,
            icon: 'warning'
          })
        }
      });
    }

    submitpassport(formpassport: FormGroup) {
      this.patchpassport();

      const formData = new FormData();

      console.log(this.agentAttributeListForm);
      const idItemsChecks = this.agentAttributeListForm.reduce((obj, item) => {

        if (item.value === null || item.value.trim() === '') {
          return false;
        }

         obj[item.label] = item.value;
        return obj;

      }, {});

      if (idItemsChecks === false) {
        console.error('Please enter values for all attributes');
        this.showMessage = "Please enter values for Mandatory Field";
      } else {
        console.log('CrimnalGlobalAttributeValues:', idItemsChecks);
      }

      //  delete agentAttributeValues.value
      this.idItemsAgentAttributeCheckMapped = {...this.formpassport.value, ...idItemsChecks}

      // const finalValues = JSON.stringify(this.educationAgentAttributeCheckMapped);

      // console.log("finalValues",finalValues)

      console.log(" CrimnalGlobalAttributeValues:::", this.crimnalGlobalAgentAttributeCheckMapped);

      console.warn("CrimnalGlobalAttributeValues===>",idItemsChecks);

        const mergedData = {

            ...this.formpassport.value,

          ...this.idItemsAgentAttributeCheckMapped,

        };

          //  formData.append('vendorchecks', JSON.stringify(this.forAddressCrimnalGlobal.value ))

          //  formData.append('vendorchecks', JSON.stringify(agentAttributeValues ))




        //  formData.append('vendorchecks', JSON.stringify(this.forAddressCrimnalGlobal.value ))
           formData.append('vendorchecks', JSON.stringify(mergedData));

         console.warn("mergedData++++++++++++++++++++",mergedData)


      if (formpassport.valid && idItemsChecks !== false) {
        console.log("....................", formpassport.value);

        // const formData = new FormData();
        formData.append('vendorchecks', JSON.stringify(formpassport.value));
        formData.append('file', this.proofDocumentNew);

        return this.customers.saveInitiateVendorChecks(formData).subscribe((result: any) => {
          console.log(result);
          if (result.outcome === true) {
            Swal.fire({
              title: result.message,
              icon: 'success'
            }).then((result) => {
              if (result.isConfirmed) {
                 window.location.reload();
              }
            });
          } else {
            Swal.fire({
              title: result.message,
              icon: 'warning'
            });
          }
        });
      } else {
        Swal.fire({
          title: 'Please enter the required details.',
          icon: 'warning'
        });

        // Add the return statement here to satisfy TypeScript
        return undefined;
      }
    }



    getsourceid(id:any){
      this.sourceid=id;

      const sourceIdString = this.sourceid;
      const sourceIdInt = parseInt(sourceIdString, 10);

      console.log(this.sourceid,"**********************")
      // const sourceName = this.getbgv.map((item: any) => item.sourceName);
      // console.warn("ALL SOURCE NAMES:", sourceName);

      const foundItem = this.getbgv.find((item: any) => item.sourceId === sourceIdInt);

      if (foundItem) {
        const correspondingSourceName = foundItem.sourceName;

        if(( correspondingSourceName && correspondingSourceName.toLowerCase().trim().includes("employment") || this.sourceid == "1")){
          this.Employments=true;
          this.education=false;
          this.GlobalDatabasecheck=false;
          this.Address=false;
          this.IDItems=false;
          this.crimnal=false;
          this.DrugTest=false;
        }
        if(( correspondingSourceName && correspondingSourceName.toLowerCase().trim().includes("education")) || (this.sourceid == "2")){
          this.education=true;
          this.Employments=false;
          this.GlobalDatabasecheck=false;
          this.Address=false;
          this.IDItems=false;
          this.crimnal=false;
          this.DrugTest=false;
        }
        if(( correspondingSourceName && correspondingSourceName.toLowerCase().trim().includes("global")) || (this.sourceid == "3")){
          this.GlobalDatabasecheck=true;
          this.Employments=false;
          this.education=false;
          this.Address=false;
          this.IDItems=false;
          this.crimnal=false;
          this.DrugTest=false;
        }
        if(( correspondingSourceName && correspondingSourceName.toLowerCase().trim().includes("address")) || (this.sourceid == "4")){
          console.warn("ADDRESS TRIGGERD::")
          this.Address=true;
          this.Employments=false;
          this.education=false;
          this.GlobalDatabasecheck=false;
          this.IDItems=false;
          this.crimnal=false;
          this.DrugTest=false;
        }
        if(( correspondingSourceName && correspondingSourceName.toLowerCase().trim().includes("id")) || (this.sourceid == "5")){
          this.IDItems=true;
          this.Employments=false;
          this.education=false;
          this.GlobalDatabasecheck=false;
          this.Address=false;
          this.crimnal=false;
          this.DrugTest=false;
        }
        if(( correspondingSourceName && correspondingSourceName.toLowerCase().trim().includes("criminal")) || (this.sourceid == "6")){
          this.crimnal=true;
          this.Employments=false;
          this.education=false;
          this.GlobalDatabasecheck=false;
          this.Address=false;
          this.IDItems=false;
          this.DrugTest=false;
        }
        if(( correspondingSourceName && correspondingSourceName.toLowerCase().trim().includes("drug")) || (this.sourceid == "10")){
          this.DrugTest=true;
          this.Employments=false;
          this.education=false;
          this.GlobalDatabasecheck=false;
          this.Address=false;
          this.IDItems=false;
          this.crimnal=false;
        }
        if(( correspondingSourceName && correspondingSourceName.toLowerCase().trim().includes("physical")) || (this.sourceid == "9")){
          this.PhysicalVisit=true;
          this.DrugTest=false;
          this.Employments=false;
          this.education=false;
          this.GlobalDatabasecheck=false;
          this.Address=false;
          this.IDItems=false;
          this.crimnal=false;
        }
        // if(this.sourceid == "25"){
        //   this.DrugTest=false;
        //   this.Employments=false;
        //   this.education=true;
        //   this.GlobalDatabasecheck=false;
        //   this.Address=false;
        //   this.IDItems=false;
        //   this.crimnal=false;
        // }


      } else {
        console.log("SourceId not found in getbgv array");
      }


    }

    opentemplate(id: any) {

      console.warn("IDDDDD::",id);
      console.warn("SourceID:::",this.sourceid);
     // this is the code for Fetching the AgentAttributesList

      this.customers.getAgentAttributes(this.sourceid).subscribe((data:any)=>{        //console.warn("CheckId::",this.checkId);
        this.AgentAttributeCheck = data.data;
        console.warn("VendorAttribute::",data)
        console.warn("ATTRIBUTE:::",this.AgentAttributeCheck.agentAttributeList);
        this.agentAttributeListForm = this.AgentAttributeCheck.agentAttributeList.map((ele: any) => {
                      return {

                        label: ele,

                        value: null

                      };

                    });

        console.log(this.agentAttributeListForm);

                });

      this.modalService.open(id, {ariaLabelledBy: 'modal-basic-title'}).result.then((res) => {
        this.closeModal = `Closed with: ${res}`;
      }, (res) => {
        this.closeModal = `Dismissed ${this.getDismissReason(res)}`;
      });

    }

    private getDismissReason(reason: any): string {
      if (reason === ModalDismissReasons.ESC) {
        return 'by pressing ESC';
      } else if (reason === ModalDismissReasons.BACKDROP_CLICK) {
        return 'by clicking on a backdrop';
      } else {
        return  `with: ${reason}`;
      }
    }

      billUpdate() {
        console.log("______________inside button ------------------")
        this.getBillValues.forEach((element:any) => {
          // element.ratePerItem = $(".billrpp"+element.source.sourceId).val();
          // element.tatPerItem = $(".billrpi"+element.source.sourceId).val();
          // element.serviceId = $(".billServiceId"+element.source.userId).val();

          const ratePerItem = document.querySelector(".billrpp" + element.source.sourceId) as HTMLInputElement;

        const tatPerItem = document.querySelector(".billrpi" + element.source.sourceId) as HTMLInputElement;

        const serviceId = document.querySelector(".billServiceId" + element.source.sourceId) as HTMLInputElement;



        if (ratePerItem && tatPerItem && serviceId) {

          element.ratePerItem = ratePerItem.value;

          //console.log(element.ratePerReport);

          element.tatPerItem = tatPerItem.value;

          //console.log(element.ratePerItem);

          element.serviceId = serviceId.value;

          //console.log(element.serviceId);

        }

        });
        return this.customers.saveVendorChecks(this.getBillValues,this.userID ).subscribe((result:any)=>{
          console.log(result,'--------------------return---------------');
          if(result.outcome === true){
            Swal.fire({
              title: result.message,
              icon: 'success'
            }).then((result) => {
              if (result.isConfirmed) {
                const navURL = 'admin/addvendor';
                this.navRouter.navigate([navURL]);
              }
            });
          }else{
            Swal.fire({
              title: result.message,
              icon: 'warning'
            })
          }
        });
      }



      updateVendorSelectModal(content: any, item: any) {
        const modalRef = this.modalService.open(content);
        let elementById = document.getElementById("updateVendorSubmit");
        if (elementById) {
          const self = this;
          const vendorId = item.vendorId;
          const vendorCheckIds = item.vendorCheckId;
          this.updateVendorForm.patchValue({
            vendorId : vendorId,
            vendorcheckId: item.vendorcheckId
          });
          elementById.addEventListener("click", function () {
            }
          );
        }
      }

      closeStatusModal(modal: any) {
        modal.dismiss('Cross click');
        // window.location.reload();
      }

      vendorUpdate(vendorUpdateForm:FormGroup){
        console.warn("vendorUpdateForm:::::::::",vendorUpdateForm.value)
        return this.customers.updateVendor(vendorUpdateForm.value).subscribe((result:any)=>{

          console.log(result);
          if(result.outcome === true){
            Swal.fire({
              title: result.message,
              icon: 'success'
            }).then((result) => {
              if (result.isConfirmed) {
                window.location.reload();
              }
            });
          }else{
            Swal.fire({
              title: result.message,
              icon: 'warning'
            })
          }
        });
      }


      toggleState(item: any) {

        console.warn("ITEM . ",item.stopCheck)
        console.warn("vendorCheckId===",item.vendorcheckId)

        const requestData = {
          vendorcheckId : item.vendorcheckId,
          stopCheck : item.stopCheck
        }
        this.customers.stopCheck(requestData).subscribe((result:any) => {

          console.log(result);
          if(result.outcome === true){
            Swal.fire({
              title: result.message,
              icon: 'success'
            }).then((result) => {
              if (result.isConfirmed) {
                window.location.reload();
              }
            });
          }else{
            Swal.fire({
              title: result.message,
              icon: 'warning'
            })
          }
        })

      }

  }

