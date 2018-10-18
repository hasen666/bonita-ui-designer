function PbModalContainerCtrl($scope, $log, $modal, modalService) {
    var ctrl = this;
    $scope.currentModal = this;    
    modalService.register($scope.properties.modalId, this);

    var modalInstances =[];
    ctrl.close = function(){
      if(modalInstances){
        modalInstances.pop().close();
      }else{
        console.log('No instance of modal to close');
      }
    }
    ctrl.open=function (){
         var modalInstance = $modal.open({
              scope: $scope,
              animation: $scope.properties.animation,
              ariaLabelledBy: 'modal-title',
              ariaDescribedBy: 'modal-body',
              templateUrl: $scope.properties.modalId+'-modalContent.html',
              appendTo: undefined
             });
         modalInstances.push(modalInstance);
      }
}
